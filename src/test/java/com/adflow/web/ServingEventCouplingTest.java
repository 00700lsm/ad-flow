package com.adflow.web;

import com.adflow.event.AdEvent;
import com.adflow.event.AdEventRepository;
import com.adflow.event.AdEventService;
import com.adflow.event.AdEventType;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.hikari.maximum-pool-size=1",
        "spring.datasource.hikari.connection-timeout=8000"
})
@AutoConfigureMockMvc
@Import(ServingEventCouplingTest.SlowEventInsert.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ServingEventCouplingTest {

    static final long HOLD_MS = 400L;
    static final CountDownLatch CONNECTION_HELD = new CountDownLatch(1);

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void getAdsWaitsWhileEventInsertHoldsConnection() throws Exception {
        int campaignId = createCampaign();
        int creativeId = addCreative(campaignId);

        AtomicLong getElapsedMs = new AtomicLong();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> impression = pool.submit(() -> {
                try {
                    mockMvc.perform(post("/events/impression")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                              "eventId": "couple-1",
                                              "campaignId": %d,
                                              "creativeId": %d,
                                              "userId": 1,
                                              "contentId": 1
                                            }
                                            """.formatted(campaignId, creativeId)))
                            .andExpect(status().isCreated());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Future<?> select = pool.submit(() -> {
                try {
                    if (!CONNECTION_HELD.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("event insert did not hold a connection");
                    }
                    long started = System.nanoTime();
                    mockMvc.perform(get("/ads").param("userId", "1").param("contentId", "1"))
                            .andExpect(status().isOk());
                    getElapsedMs.set(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            impression.get(10, TimeUnit.SECONDS);
            select.get(10, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }

        long waitMs = getElapsedMs.get();
        Files.writeString(
                Path.of(".agent/artifacts/T4-01/measurement.txt"),
                "eventHoldMs=%d getAdsWaitMs=%d pool=1%n".formatted(HOLD_MS, waitMs)
        );

        assertThat(waitMs)
                .as("GET /ads should wait while impression holds the only DB connection")
                .isGreaterThanOrEqualTo(300L);
    }

    @TestConfiguration
    static class SlowEventInsert {
        @Bean
        @Primary
        AdEventService slowEventInsert(AdEventRepository events, PlatformTransactionManager transactions) {
            TransactionTemplate tx = new TransactionTemplate(transactions);
            return new AdEventService(events) {
                @Override
                public AdEvent record(
                        String eventId,
                        Long campaignId,
                        Long creativeId,
                        Long userId,
                        Long contentId,
                        AdEventType type,
                        Instant occurredAt
                ) {
                    return tx.execute(status -> {
                        AdEvent saved = events.save(AdEvent.record(
                                eventId, campaignId, creativeId, userId, contentId, type, occurredAt));
                        CONNECTION_HELD.countDown();
                        try {
                            Thread.sleep(HOLD_MS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException(e);
                        }
                        return saved;
                    });
                }
            };
        }
    }

    private int createCampaign() throws Exception {
        String response = mockMvc.perform(post("/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "coupling",
                                  "budget": 50000,
                                  "startAt": "2026-01-01T00:00:00Z",
                                  "endAt": "2026-12-31T23:59:59Z",
                                  "priority": 5,
                                  "targetAgeMin": 20,
                                  "targetAgeMax": 39,
                                  "targetCategory": "스포츠",
                                  "frequencyCap": 0
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return json.readTree(response).get("id").asInt();
    }

    private int addCreative(int campaignId) throws Exception {
        String response = mockMvc.perform(post("/campaigns/" + campaignId + "/creatives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "CARD",
                                  "mediaUrl": "/ads/iphone.svg",
                                  "clickUrl": "https://example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return json.readTree(response).get("id").asInt();
    }
}

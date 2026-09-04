package com.adflow.web;

import com.adflow.event.AdEventRepository;
import com.adflow.event.AdEventType;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FrequencyCapRaceTest {

    private static final int CAP = 1;
    private static final int THREADS = 16;
    private static final long USER_ID = 1L;
    private static final long CONTENT_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdEventRepository events;

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void concurrentSelectThenImpressionExceedsCap() throws Exception {
        int campaignId = createCampaign("race-cap", 100, CAP);
        addCreative(campaignId, "/race.png");

        AtomicInteger selected = new AtomicInteger();
        AtomicInteger impressionsPosted = new AtomicInteger();
        int[] campaignIds = new int[THREADS];
        int[] creativeIds = new int[THREADS];
        int[] statuses = new int[THREADS];

        CountDownLatch startGet = new CountDownLatch(1);
        CountDownLatch getsDone = new CountDownLatch(THREADS);
        CountDownLatch startPost = new CountDownLatch(1);
        CountDownLatch postsDone = new CountDownLatch(THREADS);

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        try {
            for (int i = 0; i < THREADS; i++) {
                final int index = i;
                pool.submit(() -> {
                    try {
                        startGet.await();
                        MvcResult getResult = mockMvc.perform(get("/ads")
                                        .param("userId", String.valueOf(USER_ID))
                                        .param("contentId", String.valueOf(CONTENT_ID)))
                                .andReturn();
                        int status = getResult.getResponse().getStatus();
                        statuses[index] = status;
                        if (status == 200) {
                            var body = json.readTree(getResult.getResponse().getContentAsString());
                            campaignIds[index] = body.get("campaignId").asInt();
                            creativeIds[index] = body.get("creativeId").asInt();
                            selected.incrementAndGet();
                        }
                    } catch (Exception e) {
                        statuses[index] = -1;
                    } finally {
                        getsDone.countDown();
                    }
                    try {
                        startPost.await();
                        if (statuses[index] == 200) {
                            mockMvc.perform(post("/events/impression")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content("""
                                                    {
                                                      "eventId": "race-%d",
                                                      "campaignId": %d,
                                                      "creativeId": %d,
                                                      "userId": %d,
                                                      "contentId": %d
                                                    }
                                                    """.formatted(
                                                    index,
                                                    campaignIds[index],
                                                    creativeIds[index],
                                                    USER_ID,
                                                    CONTENT_ID)))
                                    .andExpect(status().isCreated());
                            impressionsPosted.incrementAndGet();
                        }
                    } catch (Exception ignored) {
                    } finally {
                        postsDone.countDown();
                    }
                });
            }

            startGet.countDown();
            assertThat(getsDone.await(30, TimeUnit.SECONDS)).isTrue();
            startPost.countDown();
            assertThat(postsDone.await(30, TimeUnit.SECONDS)).isTrue();
        } finally {
            pool.shutdownNow();
        }

        Instant now = Instant.now();
        LocalDate day = now.atZone(ZoneOffset.UTC).toLocalDate();
        Instant start = day.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        long todayImpressions = events.countByUserIdAndCampaignIdAndTypeAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(
                USER_ID,
                (long) campaignId,
                AdEventType.IMPRESSION,
                start,
                end
        );
        long overflow = todayImpressions - CAP;

        String line = "requests=%d selected=%d impressions=%d cap=%d overflow=%d".formatted(
                THREADS, selected.get(), todayImpressions, CAP, overflow);
        Files.writeString(Path.of(".agent/artifacts/T2-02/measurement.txt"), line + "\n", StandardCharsets.UTF_8);

        assertThat(todayImpressions)
                .as(line)
                .isGreaterThan(CAP);
    }

    private int createCampaign(String name, int priority, int frequencyCap) throws Exception {
        String body = """
                {
                  "name": "%s",
                  "budget": 50000,
                  "startAt": "2026-01-01T00:00:00Z",
                  "endAt": "2026-12-31T23:59:59Z",
                  "priority": %d,
                  "targetAgeMin": 20,
                  "targetAgeMax": 39,
                  "targetCategory": "스포츠",
                  "frequencyCap": %d
                }
                """.formatted(name, priority, frequencyCap);
        String response = mockMvc.perform(post("/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return json.readTree(response).get("id").asInt();
    }

    private void addCreative(int campaignId, String mediaUrl) throws Exception {
        mockMvc.perform(post("/campaigns/" + campaignId + "/creatives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "CARD",
                                  "mediaUrl": "%s",
                                  "clickUrl": "https://example.com"
                                }
                                """.formatted(mediaUrl)))
                .andExpect(status().isCreated());
    }
}

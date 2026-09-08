package com.adflow.web;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
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
class BudgetRaceTest {

    private static final long BUDGET = 1L;
    private static final int THREADS = 16;
    private static final long USER_ID = 1L;
    private static final long CONTENT_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void concurrentSelectThenImpressionExceedsBudget() throws Exception {
        int campaignId = createCampaign("race-budget", 100, BUDGET);
        int creativeId = addCreative(campaignId, "/race.png");

        int[] statuses = new int[THREADS];
        AtomicInteger selected = new AtomicInteger();
        AtomicInteger impressions = new AtomicInteger();

        CountDownLatch startGet = new CountDownLatch(1);
        CountDownLatch getsDone = new CountDownLatch(THREADS);

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
                            selected.incrementAndGet();
                        }
                    } catch (Exception e) {
                        statuses[index] = -1;
                    } finally {
                        getsDone.countDown();
                    }
                });
            }

            startGet.countDown();
            assertThat(getsDone.await(30, TimeUnit.SECONDS)).isTrue();
        } finally {
            pool.shutdownNow();
        }

        for (int i = 0; i < THREADS; i++) {
            if (statuses[i] == 200) {
                postImpression("imp-" + i, campaignId, creativeId);
                impressions.incrementAndGet();
            }
        }

        MvcResult campaign = mockMvc.perform(get("/campaigns/" + campaignId))
                .andExpect(status().isOk())
                .andReturn();
        long spent = json.readTree(campaign.getResponse().getContentAsString())
                .get("spentBudget")
                .asLong();
        long overflow = Math.max(spent - BUDGET, selected.get() - BUDGET);

        Files.writeString(
                Path.of(".agent/artifacts/T3-02/measurement.txt"),
                "requests=%d selected=%d impressions=%d budget=%d spent=%d overflow=%d%n".formatted(
                        THREADS, selected.get(), impressions.get(), BUDGET, spent, overflow
                )
        );

        assertThat(selected.get()).isGreaterThan((int) BUDGET);
        assertThat(spent).isGreaterThan(BUDGET);
    }

    private int createCampaign(String name, int priority, long budget) throws Exception {
        String body = """
                {
                  "name": "%s",
                  "budget": %d,
                  "startAt": "2026-01-01T00:00:00Z",
                  "endAt": "2026-12-31T23:59:59Z",
                  "priority": %d,
                  "targetAgeMin": 20,
                  "targetAgeMax": 39,
                  "targetCategory": "스포츠",
                  "frequencyCap": 0
                }
                """.formatted(name, budget, priority);
        String response = mockMvc.perform(post("/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return json.readTree(response).get("id").asInt();
    }

    private int addCreative(int campaignId, String mediaUrl) throws Exception {
        String response = mockMvc.perform(post("/campaigns/" + campaignId + "/creatives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "CARD",
                                  "mediaUrl": "%s",
                                  "clickUrl": "https://example.com"
                                }
                                """.formatted(mediaUrl)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return json.readTree(response).get("id").asInt();
    }

    private void postImpression(String eventId, int campaignId, int creativeId) throws Exception {
        mockMvc.perform(post("/events/impression")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "%s",
                                  "campaignId": %d,
                                  "creativeId": %d,
                                  "userId": %d,
                                  "contentId": %d
                                }
                                """.formatted(eventId, campaignId, creativeId, USER_ID, CONTENT_ID)))
                .andExpect(status().isCreated());
    }
}

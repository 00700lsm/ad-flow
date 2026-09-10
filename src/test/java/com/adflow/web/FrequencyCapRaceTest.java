package com.adflow.web;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:freqrace;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"
})
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
    private JdbcTemplate jdbcTemplate;

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void concurrentSelectDoesNotExceedCap() throws Exception {
        int campaignId = createCampaign("race-cap", 100, CAP);
        addCreative(campaignId, "/race.png");

        AtomicInteger selected = new AtomicInteger();
        int[] statuses = new int[THREADS];

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

        long todayCap = todayCapCount(USER_ID, campaignId);
        assertThat(selected.get()).isLessThanOrEqualTo(CAP);
        assertThat(todayCap).isLessThanOrEqualTo(CAP);
    }

    private long todayCapCount(long userId, long campaignId) {
        LocalDate day = LocalDate.now(ZoneOffset.UTC);
        List<Long> rows = jdbcTemplate.query(
                "select cap_count from frequency_cap_counts where user_id = ? and campaign_id = ? and utc_day = ?",
                (rs, rowNum) -> rs.getLong(1),
                userId,
                campaignId,
                day
        );
        return rows.isEmpty() ? 0 : rows.getFirst();
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

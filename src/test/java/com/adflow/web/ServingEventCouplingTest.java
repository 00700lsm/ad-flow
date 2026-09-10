package com.adflow.web;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "adflow.event.persist-delay-ms=400",
        "spring.datasource.url=jdbc:h2:mem:servcouple;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ServingEventCouplingTest {

    private static final long HOLD_MS = 400L;

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void eventApiReturnsBeforePersistFinishes() throws Exception {
        int campaignId = createCampaign();
        int creativeId = addCreative(campaignId);

        long started = System.nanoTime();
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
        long postMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);

        Files.writeString(
                Path.of(".agent/artifacts/T4-02/measurement.txt"),
                "persistDelayMs=%d postMs=%d%n".formatted(HOLD_MS, postMs)
        );

        assertThat(postMs)
                .as("POST /events/impression should return before persist delay")
                .isLessThan(150L);

        awaitImpressions(campaignId, 1);
    }

    private void awaitImpressions(int campaignId, int expected) throws Exception {
        long deadline = System.currentTimeMillis() + 3000;
        Integer last = null;
        while (System.currentTimeMillis() < deadline) {
            String body = mockMvc.perform(get("/dashboard/campaigns/" + campaignId))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            last = json.readTree(body).get("impressions").asInt();
            if (last == expected) {
                return;
            }
            Thread.sleep(25);
        }
        assertThat(last).isEqualTo(expected);
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

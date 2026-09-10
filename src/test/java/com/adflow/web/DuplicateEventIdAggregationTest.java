package com.adflow.web;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:dupagg;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"
})
@AutoConfigureMockMvc
class DuplicateEventIdAggregationTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void sameEventIdPostedThriceAggregatesOnce() throws Exception {
        int campaignId = createCampaign();
        int creativeId = addCreative(campaignId);

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/events/impression")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "eventId": "dup-1",
                                      "campaignId": %d,
                                      "creativeId": %d,
                                      "userId": 1,
                                      "contentId": 1
                                    }
                                    """.formatted(campaignId, creativeId)))
                    .andExpect(status().isCreated());
        }

        int aggregated = awaitImpressions(campaignId, 3);

        Files.writeString(
                Path.of(".agent/artifacts/T5-03/measurement.txt"),
                "posted=3 uniqueEventIds=1 aggregated=%d%n".formatted(aggregated)
        );

        assertThat(aggregated).isOne();
    }

    private int impressions(int campaignId) throws Exception {
        String body = mockMvc.perform(get("/dashboard/campaigns/" + campaignId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return json.readTree(body).get("impressions").asInt();
    }

    private int awaitImpressions(int campaignId, int expected) throws Exception {
        long deadline = System.currentTimeMillis() + 3000;
        int last = -1;
        while (System.currentTimeMillis() < deadline) {
            last = impressions(campaignId);
            if (last == expected) {
                return last;
            }
            Thread.sleep(25);
        }
        return last;
    }

    private int createCampaign() throws Exception {
        String response = mockMvc.perform(post("/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "dup-event",
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

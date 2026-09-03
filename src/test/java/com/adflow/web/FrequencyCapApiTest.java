package com.adflow.web;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FrequencyCapApiTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void afterCapReached_selectsOtherCampaign() throws Exception {
        int high = createCampaign("high", 20, 2);
        int highCreative = addCreative(high, "/high.png");
        int low = createCampaign("low", 1, 2);
        int lowCreative = addCreative(low, "/low.png");

        mockMvc.perform(get("/ads").param("userId", "1").param("contentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaignId", is(high)));

        postImpression("imp-1", high, highCreative, 1, null);
        postImpression("imp-2", high, highCreative, 1, null);

        mockMvc.perform(get("/ads").param("userId", "1").param("contentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaignId", is(low)))
                .andExpect(jsonPath("$.creativeId", is(lowCreative)));
    }

    @Test
    void countsOnlySameUserCampaignAndUtcDay() throws Exception {
        int campaignId = createCampaign("capped", 20, 2);
        int creativeId = addCreative(campaignId, "/a.png");

        postImpression("other-user", campaignId, creativeId, 2, null);
        postClick("clk-1", campaignId, creativeId, 1);
        postImpression("yesterday", campaignId, creativeId, 1, "2020-01-01T00:00:00Z");

        mockMvc.perform(get("/ads").param("userId", "1").param("contentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaignId", is(campaignId)));

        postImpression("today-1", campaignId, creativeId, 1, null);
        postImpression("today-2", campaignId, creativeId, 1, null);

        mockMvc.perform(get("/ads").param("userId", "1").param("contentId", "1"))
                .andExpect(status().isNotFound());
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

    private void postImpression(String eventId, int campaignId, int creativeId, int userId, String occurredAt) throws Exception {
        String occurred = occurredAt == null ? "" : ", \"occurredAt\": \"%s\"".formatted(occurredAt);
        mockMvc.perform(post("/events/impression")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "%s",
                                  "campaignId": %d,
                                  "creativeId": %d,
                                  "userId": %d,
                                  "contentId": 1
                                  %s
                                }
                                """.formatted(eventId, campaignId, creativeId, userId, occurred)))
                .andExpect(status().isCreated());
    }

    private void postClick(String eventId, int campaignId, int creativeId, int userId) throws Exception {
        mockMvc.perform(post("/events/click")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "%s",
                                  "campaignId": %d,
                                  "creativeId": %d,
                                  "userId": %d,
                                  "contentId": 1
                                }
                                """.formatted(eventId, campaignId, creativeId, userId)))
                .andExpect(status().isCreated());
    }
}

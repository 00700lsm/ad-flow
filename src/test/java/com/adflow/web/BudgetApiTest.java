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
class BudgetApiTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void impressionsExhaustBudgetThenSelectsOtherCampaign() throws Exception {
        int high = createCampaign("high", 20, 2);
        addCreative(high, "/high.png");
        int low = createCampaign("low", 1, 50_000L);
        int lowCreative = addCreative(low, "/low.png");

        postImpression("imp-1", high);
        postImpression("imp-2", high);

        mockMvc.perform(get("/campaigns/" + high))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spentBudget", is(2)))
                .andExpect(jsonPath("$.status", is("BUDGET_EXHAUSTED")));

        mockMvc.perform(get("/ads").param("userId", "1").param("contentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaignId", is(low)))
                .andExpect(jsonPath("$.creativeId", is(lowCreative)));
    }

    @Test
    void clickDoesNotIncreaseSpentBudget() throws Exception {
        int campaignId = createCampaign("click-only", 20, 2);

        postImpression("imp-1", campaignId);
        mockMvc.perform(post("/events/click")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "clk-1",
                                  "campaignId": %d,
                                  "creativeId": 1,
                                  "userId": 1,
                                  "contentId": 1
                                }
                                """.formatted(campaignId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/campaigns/" + campaignId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spentBudget", is(1)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
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

    private void postImpression(String eventId, int campaignId) throws Exception {
        mockMvc.perform(post("/events/impression")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "%s",
                                  "campaignId": %d,
                                  "creativeId": 1,
                                  "userId": 1,
                                  "contentId": 1
                                }
                                """.formatted(eventId, campaignId)))
                .andExpect(status().isCreated());
    }
}

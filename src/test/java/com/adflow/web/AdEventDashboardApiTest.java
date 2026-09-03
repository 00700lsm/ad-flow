package com.adflow.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdEventDashboardApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void selectAd_thenImpressionAndClickIncreaseDashboard() throws Exception {
        MvcResult created = mockMvc.perform(post("/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "스포츠 캠페인",
                                  "budget": 50000,
                                  "startAt": "2026-01-01T00:00:00Z",
                                  "endAt": "2026-12-31T23:59:59Z",
                                  "priority": 5,
                                  "targetAgeMin": 20,
                                  "targetAgeMax": 39,
                                  "targetCategory": "스포츠",
                                  "frequencyCap": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        int campaignId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(created.getResponse().getContentAsString())
                .get("id")
                .asInt();

        MvcResult creative = mockMvc.perform(post("/campaigns/" + campaignId + "/creatives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "CARD",
                                  "mediaUrl": "/ads/iphone.svg",
                                  "clickUrl": "https://example.com/iphone"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        int creativeId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(creative.getResponse().getContentAsString())
                .get("id")
                .asInt();

        mockMvc.perform(get("/ads").param("userId", "1").param("contentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaignId", is(campaignId)))
                .andExpect(jsonPath("$.creativeId", is(creativeId)));

        mockMvc.perform(get("/ads").param("userId", "2").param("contentId", "2"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/events/impression")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "imp-1",
                                  "campaignId": %d,
                                  "creativeId": %d,
                                  "userId": 1,
                                  "contentId": 1
                                }
                                """.formatted(campaignId, creativeId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/events/click")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "clk-1",
                                  "campaignId": %d,
                                  "creativeId": %d,
                                  "userId": 1,
                                  "contentId": 1
                                }
                                """.formatted(campaignId, creativeId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/dashboard/campaigns/" + campaignId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.impressions", is(1)))
                .andExpect(jsonPath("$.clicks", is(1)))
                .andExpect(jsonPath("$.ctr", closeTo(1.0, 0.0001)));

        mockMvc.perform(get("/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaigns[0].impressions").exists());
    }
}

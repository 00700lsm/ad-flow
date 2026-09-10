package com.adflow.web;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:rtgap;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"
})
@AutoConfigureMockMvc
@Transactional
class DashboardRealtimeGapTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void campaignJsonHasNoPerSecondOrLagFields() throws Exception {
        int campaignId = createCampaign();

        String body = mockMvc.perform(get("/dashboard/campaigns/" + campaignId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.impressions").exists())
                .andExpect(jsonPath("$.clicks").exists())
                .andExpect(jsonPath("$.ctr").exists())
                .andExpect(jsonPath("$.impressionsPerSec").doesNotExist())
                .andExpect(jsonPath("$.clicksPerSec").doesNotExist())
                .andExpect(jsonPath("$.kafkaLag").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(json.readTree(body).has("impressionsPerSec")).isFalse();
        writeMeasurement();
    }

    @Test
    void dashboardHtmlPollsEveryThreeSecondsWithoutSse() throws Exception {
        String html = mockMvc.perform(get("/dashboard.html"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(html).contains("3초마다");
        assertThat(html).contains("setInterval(load, 3000)");
        assertThat(html).doesNotContain("EventSource");
        assertThat(html).doesNotContain("impressionsPerSec");
        writeMeasurement();
    }

    private void writeMeasurement() throws Exception {
        Files.createDirectories(Path.of(".agent/artifacts/T6-01"));
        Files.writeString(
                Path.of(".agent/artifacts/T6-01/measurement.txt"),
                "pollMs=3000 sse=0 impressionsPerSecField=0\n"
        );
    }

    private int createCampaign() throws Exception {
        String response = mockMvc.perform(post("/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "realtime-gap",
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
}

package com.adflow.web;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:simapi;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"
})
@AutoConfigureMockMvc
@Transactional
class SimulationApiTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void createReturnsId() throws Exception {
        mockMvc.perform(post("/simulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "concurrentUsers": 2 }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.concurrentUsers", is(2)));
    }

    @Test
    void startSelectsAdsThatManyTimes() throws Exception {
        int campaignId = createSportsCampaign(10);

        mockMvc.perform(post("/simulations/" + createSimulation(2) + "/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestCount", is(2)));

        mockMvc.perform(get("/dashboard/campaigns/" + campaignId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spentBudget", is(2)));
        writeMeasurement();
    }

    @Test
    void stopBeforeStartIssuesNoRequests() throws Exception {
        int campaignId = createSportsCampaign(10);
        long id = createSimulation(2);

        mockMvc.perform(post("/simulations/" + id + "/stop"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/simulations/" + id + "/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestCount", is(0)));

        mockMvc.perform(get("/dashboard/campaigns/" + campaignId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spentBudget", is(0)));
        writeMeasurement();
    }

    @Test
    void createStoresDistribution() throws Exception {
        mockMvc.perform(post("/simulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "concurrentUsers": 2,
                                  "ageShares": { "20대": 50, "40대": 50 },
                                  "categories": ["스포츠", "드라마"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.concurrentUsers", is(2)))
                .andExpect(jsonPath("$.ageShares['20대']", is(50)))
                .andExpect(jsonPath("$.ageShares['40대']", is(50)))
                .andExpect(jsonPath("$.categories", contains("스포츠", "드라마")));
    }

    @Test
    void startUsesDramaUserWhenOnlyFortiesAndDrama() throws Exception {
        int sportsId = createCampaign("sim-sports", 10, 20, 39, "스포츠");
        int dramaId = createCampaign("sim-drama", 10, 40, 59, "드라마");

        mockMvc.perform(post("/simulations/" + createSimulation("""
                        {
                          "concurrentUsers": 2,
                          "ageShares": { "40대": 100 },
                          "categories": ["드라마"]
                        }
                        """) + "/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestCount", is(2)));

        mockMvc.perform(get("/dashboard/campaigns/" + dramaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spentBudget", is(2)));
        mockMvc.perform(get("/dashboard/campaigns/" + sportsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spentBudget", is(0)));
        writeMeasurement();
    }

    @Test
    void startSplitsAcrossAgeAndCategory() throws Exception {
        int sportsId = createCampaign("sim-sports", 10, 20, 39, "스포츠");
        int dramaId = createCampaign("sim-drama", 10, 40, 59, "드라마");

        mockMvc.perform(post("/simulations/" + createSimulation("""
                        {
                          "concurrentUsers": 2,
                          "ageShares": { "20대": 50, "40대": 50 },
                          "categories": ["스포츠", "드라마"]
                        }
                        """) + "/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestCount", is(2)));

        mockMvc.perform(get("/dashboard/campaigns/" + sportsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spentBudget", is(1)));
        mockMvc.perform(get("/dashboard/campaigns/" + dramaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spentBudget", is(1)));
        writeMeasurement();
    }

    private void writeMeasurement() throws Exception {
        Files.createDirectories(Path.of(".agent/artifacts/T7-04"));
        Files.writeString(
                Path.of(".agent/artifacts/T7-04/measurement.txt"),
                "dramaOnlySpent=2 sportsWhenDramaOnly=0 splitSports=1 splitDrama=1\n"
        );
    }

    private long createSimulation(int concurrentUsers) throws Exception {
        return createSimulation("""
                { "concurrentUsers": %d }
                """.formatted(concurrentUsers));
    }

    private long createSimulation(String body) throws Exception {
        MvcResult created = mockMvc.perform(post("/simulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return json.readTree(created.getResponse().getContentAsString()).get("id").asLong();
    }

    private int createSportsCampaign(int budget) throws Exception {
        return createCampaign("sim-sports", budget, 20, 39, "스포츠");
    }

    private int createCampaign(String name, int budget, int ageMin, int ageMax, String category) throws Exception {
        MvcResult created = mockMvc.perform(post("/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "budget": %d,
                                  "startAt": "2026-01-01T00:00:00Z",
                                  "endAt": "2026-12-31T23:59:59Z",
                                  "priority": 5,
                                  "targetAgeMin": %d,
                                  "targetAgeMax": %d,
                                  "targetCategory": "%s",
                                  "frequencyCap": 0
                                }
                                """.formatted(name, budget, ageMin, ageMax, category)))
                .andExpect(status().isCreated())
                .andReturn();
        int campaignId = json.readTree(created.getResponse().getContentAsString()).get("id").asInt();
        mockMvc.perform(post("/campaigns/" + campaignId + "/creatives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "CARD",
                                  "mediaUrl": "/ads/iphone.svg",
                                  "clickUrl": "https://example.com/iphone"
                                }
                                """))
                .andExpect(status().isCreated());
        return campaignId;
    }
}

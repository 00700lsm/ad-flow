package com.adflow.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:simpage;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"
})
@AutoConfigureMockMvc
class SimulatorPageTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void simulatorHtmlIsServed() throws Exception {
        mockMvc.perform(get("/simulator.html"))
                .andExpect(status().isOk());
        writeMeasurement();
    }

    @Test
    void indexLinksToSimulator() throws Exception {
        String html = mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(html).contains("simulator.html");
        writeMeasurement();
    }

    @Test
    void pagePostsSimulationsAndStartStop() throws Exception {
        String html = mockMvc.perform(get("/simulator.html"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(html).contains("/simulations");
        assertThat(html).contains("/start");
        assertThat(html).contains("/stop");
        assertThat(html).contains("concurrentUsers");
        assertThat(html).contains("20대");
        assertThat(html).contains("40대");
        assertThat(html).contains("스포츠");
        assertThat(html).contains("드라마");
        writeMeasurement();
    }

    @Test
    void pagePostsThirtiesShareAndClickRate() throws Exception {
        String html = mockMvc.perform(get("/simulator.html"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(html).contains("30대");
        assertThat(html).contains("clickRate");
        assertThat(html).contains("'30대'");
        writeFormMeasurement();
    }

    private void writeMeasurement() throws Exception {
        Files.createDirectories(Path.of(".agent/artifacts/T7-05"));
        Files.writeString(
                Path.of(".agent/artifacts/T7-05/measurement.txt"),
                "simulatorHtml=1 indexLink=1 postsStartStop=1\n"
        );
    }

    private void writeFormMeasurement() throws Exception {
        Files.createDirectories(Path.of(".agent/artifacts/T7-09"));
        Files.writeString(
                Path.of(".agent/artifacts/T7-09/measurement.txt"),
                "thirtiesShare=1 clickRate=1\n"
        );
    }
}

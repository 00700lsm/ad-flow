package com.adflow.web;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SimulatorGapTest {

    @Test
    void simulatorPageIsAbsent() throws Exception {
        Path staticDir = Path.of("src/main/resources/static");
        String index = Files.readString(staticDir.resolve("index.html"), StandardCharsets.UTF_8);

        assertThat(Files.exists(staticDir.resolve("simulator.html"))).isFalse();
        assertThat(index).doesNotContain("simulator.html");
        assertThat(index).doesNotContain("Simulator");
    }
}

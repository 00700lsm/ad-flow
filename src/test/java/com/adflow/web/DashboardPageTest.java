package com.adflow.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardPageTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void dashboardHtmlShowsBudgetAndStatus() throws Exception {
        String html = mockMvc.perform(get("/dashboard.html"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(html).contains("사용 예산");
        assertThat(html).contains("잔여 예산");
        assertThat(html).contains("상태");
        assertThat(html).contains("c.spentBudget");
        assertThat(html).contains("c.remainingBudget");
        assertThat(html).contains("c.status");
    }
}

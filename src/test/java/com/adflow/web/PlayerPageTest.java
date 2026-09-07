package com.adflow.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlayerPageTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void playerHtmlHasSessionAdHistory() throws Exception {
        String html = mockMvc.perform(get("/player.html"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("id=\"adHistory\"");
        assertThat(html).contains("getElementById('adHistory')");
        assertThat(html).contains("ad.campaignName");
    }
}

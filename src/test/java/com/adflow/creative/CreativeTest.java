package com.adflow.creative;

import com.adflow.campaign.Campaign;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreativeTest {

    @Test
    void attachToCampaign_storesMediaAndClickUrl() {
        Campaign campaign = Campaign.create(
                "아이폰 신제품 광고",
                50_000L,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z"),
                10,
                20,
                39,
                "스포츠",
                2
        );
        campaign.assignId(1L);

        Creative creative = Creative.attach(campaign.getId(), "CARD", "/ads/iphone.png", "https://example.com");

        assertThat(creative.getCampaignId()).isEqualTo(1L);
        assertThat(creative.getType()).isEqualTo("CARD");
        assertThat(creative.getMediaUrl()).isEqualTo("/ads/iphone.png");
        assertThat(creative.getClickUrl()).isEqualTo("https://example.com");
    }

    @Test
    void reject_blankMediaUrl() {
        assertThatThrownBy(() -> Creative.attach(1L, "CARD", " ", "https://example.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

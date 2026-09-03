package com.adflow.ads;

import com.adflow.campaign.Campaign;
import com.adflow.content.Content;
import com.adflow.creative.Creative;
import com.adflow.user.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AdSelectionTest {

    private static final Instant NOW = Instant.parse("2026-06-01T00:00:00Z");

    private final AdSelector selector = new AdSelector();

    @Test
    void picksHighestPriorityMatching() {
        Campaign low = campaign("low", 1, 20, 39, "스포츠");
        low.assignId(1L);
        Campaign high = campaign("high", 20, 20, 39, "스포츠");
        high.assignId(2L);

        Creative lowCreative = Creative.attach(1L, "CARD", "/low.png", "https://low");
        lowCreative.assignId(11L);
        Creative highCreative = Creative.attach(2L, "CARD", "/high.png", "https://high");
        highCreative.assignId(22L);

        User user = new User(1L, 28, "스포츠");
        Content content = new Content(1L, "축구 하이라이트", "스포츠");

        Optional<SelectedAd> selected = selector.select(
                List.of(new CampaignCreative(low, lowCreative), new CampaignCreative(high, highCreative)),
                user,
                content,
                NOW
        );

        assertThat(selected).isPresent();
        assertThat(selected.get().campaignId()).isEqualTo(2L);
        assertThat(selected.get().creativeId()).isEqualTo(22L);
        assertThat(selected.get().mediaUrl()).isEqualTo("/high.png");
    }

    @Test
    void rejectsInactivePausedOutOfPeriodAgeGenre() {
        Campaign paused = campaign("paused", 50, 20, 39, "스포츠");
        paused.assignId(1L);
        paused.pause();

        Campaign wrongGenre = campaign("drama", 50, 20, 39, "드라마");
        wrongGenre.assignId(2L);

        Campaign tooOld = campaign("age", 50, 40, 49, "스포츠");
        tooOld.assignId(3L);

        Campaign expired = Campaign.create(
                "expired",
                10_000L,
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-12-31T00:00:00Z"),
                50,
                20,
                39,
                "스포츠",
                1
        );
        expired.assignId(4L);

        User user = new User(1L, 28, "스포츠");
        Content content = new Content(1L, "축구 하이라이트", "스포츠");

        List<CampaignCreative> candidates = List.of(
                new CampaignCreative(paused, Creative.attach(1L, "CARD", "/a.png", "https://a")),
                new CampaignCreative(wrongGenre, Creative.attach(2L, "CARD", "/b.png", "https://b")),
                new CampaignCreative(tooOld, Creative.attach(3L, "CARD", "/c.png", "https://c")),
                new CampaignCreative(expired, Creative.attach(4L, "CARD", "/d.png", "https://d"))
        );

        assertThat(selector.select(candidates, user, content, NOW)).isEmpty();
    }

    @Test
    void skipsCampaignWhenTodayImpressionsReachCap() {
        Campaign capped = campaign("capped", 50, 20, 39, "스포츠", 2);
        capped.assignId(1L);
        Campaign other = campaign("other", 1, 20, 39, "스포츠", 2);
        other.assignId(2L);

        User user = new User(1L, 28, "스포츠");
        Content content = new Content(1L, "축구 하이라이트", "스포츠");

        Optional<SelectedAd> selected = selector.select(
                List.of(
                        new CampaignCreative(capped, creative(1L, "/capped.png")),
                        new CampaignCreative(other, creative(2L, "/other.png"))
                ),
                user,
                content,
                NOW,
                Map.of(1L, 2L)
        );

        assertThat(selected).isPresent();
        assertThat(selected.get().campaignId()).isEqualTo(2L);
    }

    @Test
    void allowsCampaignWhenTodayImpressionsBelowCap() {
        Campaign campaign = campaign("target", 50, 20, 39, "스포츠", 2);
        campaign.assignId(1L);

        Optional<SelectedAd> selected = selector.select(
                List.of(new CampaignCreative(campaign, creative(1L, "/a.png"))),
                new User(1L, 28, "스포츠"),
                new Content(1L, "축구 하이라이트", "스포츠"),
                NOW,
                Map.of(1L, 1L)
        );

        assertThat(selected).isPresent();
        assertThat(selected.get().campaignId()).isEqualTo(1L);
    }

    @Test
    void ignoresCapWhenFrequencyCapIsZero() {
        Campaign unlimited = campaign("unlimited", 50, 20, 39, "스포츠", 0);
        unlimited.assignId(1L);

        Optional<SelectedAd> selected = selector.select(
                List.of(new CampaignCreative(unlimited, creative(1L, "/a.png"))),
                new User(1L, 28, "스포츠"),
                new Content(1L, "축구 하이라이트", "스포츠"),
                NOW,
                Map.of(1L, 100L)
        );

        assertThat(selected).isPresent();
        assertThat(selected.get().campaignId()).isEqualTo(1L);
    }

    private static Creative creative(long campaignId, String mediaUrl) {
        Creative creative = Creative.attach(campaignId, "CARD", mediaUrl, "https://example.com");
        creative.assignId(campaignId * 10);
        return creative;
    }

    private static Campaign campaign(String name, int priority, int ageMin, int ageMax, String category) {
        return campaign(name, priority, ageMin, ageMax, category, 2);
    }

    private static Campaign campaign(String name, int priority, int ageMin, int ageMax, String category, int frequencyCap) {
        return Campaign.create(
                name,
                50_000L,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z"),
                priority,
                ageMin,
                ageMax,
                category,
                frequencyCap
        );
    }
}

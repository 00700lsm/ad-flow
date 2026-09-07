package com.adflow.campaign;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CampaignTest {

    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant END = Instant.parse("2026-12-31T23:59:59Z");

    @Test
    void create_storesRequiredFields() {
        Campaign campaign = Campaign.create(
                "아이폰 신제품 광고",
                50_000L,
                START,
                END,
                10,
                20,
                39,
                "스포츠",
                2
        );

        assertThat(campaign.getName()).isEqualTo("아이폰 신제품 광고");
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.ACTIVE);
        assertThat(campaign.getBudget()).isEqualTo(50_000L);
        assertThat(campaign.getSpentBudget()).isZero();
        assertThat(campaign.getStartAt()).isEqualTo(START);
        assertThat(campaign.getEndAt()).isEqualTo(END);
        assertThat(campaign.getPriority()).isEqualTo(10);
        assertThat(campaign.getTargetAgeMin()).isEqualTo(20);
        assertThat(campaign.getTargetAgeMax()).isEqualTo(39);
        assertThat(campaign.getTargetCategory()).isEqualTo("스포츠");
        assertThat(campaign.getFrequencyCap()).isEqualTo(2);
    }

    @Test
    void pause_fromActive_and_activate_fromPaused() {
        Campaign campaign = sample();

        campaign.pause();
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.PAUSED);

        campaign.activate();
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.ACTIVE);
    }

    @Test
    void reject_blankName() {
        assertThatThrownBy(() -> Campaign.create(
                "  ",
                50_000L,
                START,
                END,
                10,
                20,
                39,
                "스포츠",
                2
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reject_endBeforeStart() {
        assertThatThrownBy(() -> Campaign.create(
                "잘못된 기간",
                50_000L,
                END,
                START,
                10,
                20,
                39,
                "스포츠",
                2
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isActiveAt_respectsStatusAndPeriod() {
        Campaign campaign = sample();
        Instant inside = Instant.parse("2026-06-01T00:00:00Z");
        Instant before = Instant.parse("2025-12-31T00:00:00Z");

        assertThat(campaign.isActiveAt(inside)).isTrue();
        assertThat(campaign.isActiveAt(before)).isFalse();

        campaign.pause();
        assertThat(campaign.isActiveAt(inside)).isFalse();
    }

    @Test
    void chargeImpression_incrementsSpentAndExhaustsWhenReached() {
        Campaign campaign = Campaign.create(
                "소진",
                2L,
                START,
                END,
                10,
                20,
                39,
                "스포츠",
                2
        );

        campaign.chargeImpression();
        assertThat(campaign.getSpentBudget()).isEqualTo(1L);
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.ACTIVE);

        campaign.chargeImpression();
        assertThat(campaign.getSpentBudget()).isEqualTo(2L);
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.BUDGET_EXHAUSTED);
        assertThat(campaign.hasRemainingBudget()).isFalse();
    }

    private static Campaign sample() {
        return Campaign.create(
                "아이폰 신제품 광고",
                50_000L,
                START,
                END,
                10,
                20,
                39,
                "스포츠",
                2
        );
    }
}

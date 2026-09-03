package com.adflow.dashboard;

public record CampaignStats(
        Long campaignId,
        String name,
        long impressions,
        long clicks,
        double ctr
) {
}

package com.adflow.dashboard;

import com.adflow.campaign.CampaignStatus;

public record CampaignStats(
        Long campaignId,
        String name,
        CampaignStatus status,
        long budget,
        long spentBudget,
        long remainingBudget,
        long impressions,
        long clicks,
        double ctr
) {
}

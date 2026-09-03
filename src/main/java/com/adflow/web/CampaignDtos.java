package com.adflow.web;

import com.adflow.campaign.Campaign;
import com.adflow.campaign.CampaignStatus;
import com.adflow.creative.Creative;

import java.time.Instant;

public final class CampaignDtos {

    private CampaignDtos() {
    }

    public record CreateRequest(
            String name,
            long budget,
            Instant startAt,
            Instant endAt,
            int priority,
            int targetAgeMin,
            int targetAgeMax,
            String targetCategory,
            int frequencyCap
    ) {
    }

    public record PatchRequest(
            String name,
            CampaignStatus status,
            Long budget,
            Instant startAt,
            Instant endAt,
            Integer priority,
            Integer targetAgeMin,
            Integer targetAgeMax,
            String targetCategory,
            Integer frequencyCap
    ) {
    }

    public record CreativeRequest(String type, String mediaUrl, String clickUrl) {
    }

    public record CampaignResponse(
            Long id,
            String name,
            CampaignStatus status,
            long budget,
            long spentBudget,
            Instant startAt,
            Instant endAt,
            int priority,
            int targetAgeMin,
            int targetAgeMax,
            String targetCategory,
            int frequencyCap
    ) {
        static CampaignResponse from(Campaign campaign) {
            return new CampaignResponse(
                    campaign.getId(),
                    campaign.getName(),
                    campaign.getStatus(),
                    campaign.getBudget(),
                    campaign.getSpentBudget(),
                    campaign.getStartAt(),
                    campaign.getEndAt(),
                    campaign.getPriority(),
                    campaign.getTargetAgeMin(),
                    campaign.getTargetAgeMax(),
                    campaign.getTargetCategory(),
                    campaign.getFrequencyCap()
            );
        }
    }

    public record CreativeResponse(Long id, Long campaignId, String type, String mediaUrl, String clickUrl) {
        static CreativeResponse from(Creative creative) {
            return new CreativeResponse(
                    creative.getId(),
                    creative.getCampaignId(),
                    creative.getType(),
                    creative.getMediaUrl(),
                    creative.getClickUrl()
            );
        }
    }
}

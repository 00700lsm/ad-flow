package com.adflow.event;

import com.adflow.campaign.Campaign;
import com.adflow.campaign.CampaignRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AdEventService {

    private final AdEventRepository events;
    private final CampaignRepository campaigns;

    public AdEventService(AdEventRepository events, CampaignRepository campaigns) {
        this.events = events;
        this.campaigns = campaigns;
    }

    @Transactional
    public AdEvent record(
            String eventId,
            Long campaignId,
            Long creativeId,
            Long userId,
            Long contentId,
            AdEventType type,
            Instant occurredAt
    ) {
        if (type == AdEventType.IMPRESSION) {
            campaigns.findById(campaignId).ifPresent(Campaign::chargeImpression);
        }
        return events.save(AdEvent.record(eventId, campaignId, creativeId, userId, contentId, type, occurredAt));
    }
}

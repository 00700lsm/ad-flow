package com.adflow.dashboard;

import com.adflow.campaign.Campaign;
import com.adflow.campaign.CampaignRepository;
import com.adflow.event.AdEventRepository;
import com.adflow.event.AdEventType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardService {

    private final CampaignRepository campaigns;
    private final AdEventRepository events;

    public DashboardService(CampaignRepository campaigns, AdEventRepository events) {
        this.campaigns = campaigns;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public CampaignStats stats(Long campaignId) {
        Campaign campaign = campaigns.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("캠페인을 찾을 수 없습니다: " + campaignId));
        return toStats(campaign);
    }

    @Transactional(readOnly = true)
    public List<CampaignStats> summary() {
        return campaigns.findAll().stream().map(this::toStats).toList();
    }

    private CampaignStats toStats(Campaign campaign) {
        long impressions = events.countByCampaignIdAndType(campaign.getId(), AdEventType.IMPRESSION);
        long clicks = events.countByCampaignIdAndType(campaign.getId(), AdEventType.CLICK);
        double ctr = impressions == 0 ? 0.0 : (double) clicks / impressions;
        return new CampaignStats(campaign.getId(), campaign.getName(), impressions, clicks, ctr);
    }
}

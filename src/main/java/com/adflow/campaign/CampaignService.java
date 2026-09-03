package com.adflow.campaign;

import com.adflow.creative.Creative;
import com.adflow.creative.CreativeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class CampaignService {

    private final CampaignRepository campaigns;
    private final CreativeRepository creatives;

    public CampaignService(CampaignRepository campaigns, CreativeRepository creatives) {
        this.campaigns = campaigns;
        this.creatives = creatives;
    }

    @Transactional
    public Campaign create(
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
        return campaigns.save(Campaign.create(
                name,
                budget,
                startAt,
                endAt,
                priority,
                targetAgeMin,
                targetAgeMax,
                targetCategory,
                frequencyCap
        ));
    }

    @Transactional(readOnly = true)
    public List<Campaign> list() {
        return campaigns.findAll();
    }

    @Transactional(readOnly = true)
    public Campaign get(Long id) {
        return campaigns.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("캠페인을 찾을 수 없습니다: " + id));
    }

    @Transactional
    public Campaign patch(
            Long id,
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
        Campaign campaign = get(id);
        campaign.update(name, budget, startAt, endAt, priority, targetAgeMin, targetAgeMax, targetCategory, frequencyCap);
        if (status != null && status != campaign.getStatus()) {
            if (status == CampaignStatus.PAUSED) {
                campaign.pause();
            } else if (status == CampaignStatus.ACTIVE) {
                campaign.activate();
            } else {
                throw new IllegalArgumentException("Phase 1에서 변경할 수 없는 상태입니다: " + status);
            }
        }
        return campaign;
    }

    @Transactional
    public Creative addCreative(Long campaignId, String type, String mediaUrl, String clickUrl) {
        Campaign campaign = get(campaignId);
        return creatives.save(Creative.attach(campaign.getId(), type, mediaUrl, clickUrl));
    }
}

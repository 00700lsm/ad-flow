package com.adflow.ads;

import com.adflow.campaign.Campaign;
import com.adflow.campaign.CampaignRepository;
import com.adflow.content.Content;
import com.adflow.content.ContentRepository;
import com.adflow.creative.Creative;
import com.adflow.creative.CreativeRepository;
import com.adflow.user.User;
import com.adflow.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdServingService {

    private final CampaignRepository campaigns;
    private final CreativeRepository creatives;
    private final UserRepository users;
    private final ContentRepository contents;
    private final FrequencyCapCounter frequencyCaps;
    private final AdSelector selector = new AdSelector();

    public AdServingService(
            CampaignRepository campaigns,
            CreativeRepository creatives,
            UserRepository users,
            ContentRepository contents,
            FrequencyCapCounter frequencyCaps
    ) {
        this.campaigns = campaigns;
        this.creatives = creatives;
        this.users = users;
        this.contents = contents;
        this.frequencyCaps = frequencyCaps;
    }

    @Transactional
    public SelectedAd select(Long userId, Long contentId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        Content content = contents.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("콘텐츠를 찾을 수 없습니다: " + contentId));

        List<Campaign> all = campaigns.findAll();
        Map<Long, List<Creative>> byCampaign = creatives.findAll().stream()
                .collect(Collectors.groupingBy(Creative::getCampaignId));

        Instant now = Instant.now();
        LocalDate utcDay = now.atZone(ZoneOffset.UTC).toLocalDate();

        List<CampaignCreative> remaining = new ArrayList<>();
        for (Campaign campaign : all) {
            List<Creative> campaignCreatives = byCampaign.getOrDefault(campaign.getId(), List.of());
            for (Creative creative : campaignCreatives) {
                remaining.add(new CampaignCreative(campaign, creative));
            }
        }

        while (!remaining.isEmpty()) {
            SelectedAd selected = selector.select(remaining, user, content, now, Map.of())
                    .orElseThrow(() -> new AdNotFoundException("조건에 맞는 광고가 없습니다"));
            Campaign campaign = campaignOf(remaining, selected.campaignId());
            if (campaign.getFrequencyCap() <= 0
                    || frequencyCaps.tryIncrement(userId, campaign.getId(), utcDay, campaign.getFrequencyCap())) {
                return selected;
            }
            remaining = remaining.stream()
                    .filter(item -> !item.campaign().getId().equals(campaign.getId()))
                    .collect(Collectors.toList());
        }
        throw new AdNotFoundException("조건에 맞는 광고가 없습니다");
    }

    private static Campaign campaignOf(List<CampaignCreative> remaining, Long campaignId) {
        return remaining.stream()
                .map(CampaignCreative::campaign)
                .filter(campaign -> campaign.getId().equals(campaignId))
                .findFirst()
                .orElseThrow(() -> new AdNotFoundException("조건에 맞는 광고가 없습니다"));
    }
}

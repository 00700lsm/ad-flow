package com.adflow.ads;

import com.adflow.campaign.Campaign;
import com.adflow.campaign.CampaignRepository;
import com.adflow.content.Content;
import com.adflow.content.ContentRepository;
import com.adflow.creative.Creative;
import com.adflow.creative.CreativeRepository;
import com.adflow.event.AdEventRepository;
import com.adflow.event.AdEventType;
import com.adflow.user.User;
import com.adflow.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdServingService {

    private final CampaignRepository campaigns;
    private final CreativeRepository creatives;
    private final UserRepository users;
    private final ContentRepository contents;
    private final AdEventRepository events;
    private final AdSelector selector = new AdSelector();

    public AdServingService(
            CampaignRepository campaigns,
            CreativeRepository creatives,
            UserRepository users,
            ContentRepository contents,
            AdEventRepository events
    ) {
        this.campaigns = campaigns;
        this.creatives = creatives;
        this.users = users;
        this.contents = contents;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public SelectedAd select(Long userId, Long contentId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        Content content = contents.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("콘텐츠를 찾을 수 없습니다: " + contentId));

        List<Campaign> all = campaigns.findAll();
        Map<Long, List<Creative>> byCampaign = creatives.findAll().stream()
                .collect(Collectors.groupingBy(Creative::getCampaignId));

        Instant now = Instant.now();
        Map<Long, Long> todayImpressions = todayImpressions(userId, all, now);

        List<CampaignCreative> candidates = new ArrayList<>();
        for (Campaign campaign : all) {
            List<Creative> campaignCreatives = byCampaign.getOrDefault(campaign.getId(), List.of());
            for (Creative creative : campaignCreatives) {
                candidates.add(new CampaignCreative(campaign, creative));
            }
        }

        return selector.select(candidates, user, content, now, todayImpressions)
                .orElseThrow(() -> new AdNotFoundException("조건에 맞는 광고가 없습니다"));
    }

    private Map<Long, Long> todayImpressions(Long userId, List<Campaign> all, Instant now) {
        LocalDate day = now.atZone(ZoneOffset.UTC).toLocalDate();
        Instant start = day.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        Map<Long, Long> counts = new HashMap<>();
        for (Campaign campaign : all) {
            long count = events.countByUserIdAndCampaignIdAndTypeAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(
                    userId,
                    campaign.getId(),
                    AdEventType.IMPRESSION,
                    start,
                    end
            );
            if (count > 0) {
                counts.put(campaign.getId(), count);
            }
        }
        return counts;
    }
}

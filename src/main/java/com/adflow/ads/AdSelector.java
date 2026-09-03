package com.adflow.ads;

import com.adflow.campaign.Campaign;
import com.adflow.content.Content;
import com.adflow.user.User;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdSelector {

    public Optional<SelectedAd> select(
            List<CampaignCreative> candidates,
            User user,
            Content content,
            Instant now
    ) {
        return select(candidates, user, content, now, Map.of());
    }

    public Optional<SelectedAd> select(
            List<CampaignCreative> candidates,
            User user,
            Content content,
            Instant now,
            Map<Long, Long> todayImpressions
    ) {
        Map<Long, Long> counts = todayImpressions != null ? todayImpressions : Map.of();
        return candidates.stream()
                .filter(item -> item.campaign().isActiveAt(now))
                .filter(item -> item.campaign().matches(user.getAge(), content.getCategory()))
                .filter(item -> withinFrequencyCap(item.campaign(), counts))
                .max(Comparator
                        .comparingInt((CampaignCreative item) -> item.campaign().getPriority())
                        .thenComparing(item -> item.campaign().getId(), Comparator.nullsLast(Long::compareTo).reversed()))
                .map(item -> new SelectedAd(
                        item.campaign().getId(),
                        item.creative().getId(),
                        item.campaign().getName(),
                        item.creative().getType(),
                        item.creative().getMediaUrl(),
                        item.creative().getClickUrl()
                ));
    }

    private static boolean withinFrequencyCap(Campaign campaign, Map<Long, Long> todayImpressions) {
        int cap = campaign.getFrequencyCap();
        if (cap <= 0) {
            return true;
        }
        long count = todayImpressions.getOrDefault(campaign.getId(), 0L);
        return count < cap;
    }
}

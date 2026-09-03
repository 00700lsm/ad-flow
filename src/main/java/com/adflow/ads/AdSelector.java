package com.adflow.ads;

import com.adflow.content.Content;
import com.adflow.user.User;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AdSelector {

    public Optional<SelectedAd> select(
            List<CampaignCreative> candidates,
            User user,
            Content content,
            Instant now
    ) {
        return candidates.stream()
                .filter(item -> item.campaign().isActiveAt(now))
                .filter(item -> item.campaign().matches(user.getAge(), content.getCategory()))
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
}

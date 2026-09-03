package com.adflow.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface AdEventRepository extends JpaRepository<AdEvent, Long> {
    long countByCampaignIdAndType(Long campaignId, AdEventType type);

    long countByUserIdAndCampaignIdAndTypeAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(
            Long userId,
            Long campaignId,
            AdEventType type,
            Instant startInclusive,
            Instant endExclusive
    );
}

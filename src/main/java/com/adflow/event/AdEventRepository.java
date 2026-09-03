package com.adflow.event;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdEventRepository extends JpaRepository<AdEvent, Long> {
    long countByCampaignIdAndType(Long campaignId, AdEventType type);
}

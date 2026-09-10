package com.adflow.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "ad_events")
public class AdEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;
    private Long campaignId;
    private Long creativeId;
    private Long userId;
    private Long contentId;

    @Enumerated(EnumType.STRING)
    private AdEventType type;

    private Instant occurredAt;

    protected AdEvent() {
    }

    public static AdEvent record(
            String eventId,
            Long campaignId,
            Long creativeId,
            Long userId,
            Long contentId,
            AdEventType type,
            Instant occurredAt
    ) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId는 필수입니다");
        }
        if (campaignId == null || creativeId == null || userId == null || contentId == null) {
            throw new IllegalArgumentException("campaignId, creativeId, userId, contentId는 필수입니다");
        }
        AdEvent event = new AdEvent();
        event.eventId = eventId.trim();
        event.campaignId = campaignId;
        event.creativeId = creativeId;
        event.userId = userId;
        event.contentId = contentId;
        event.type = type;
        event.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        return event;
    }

    public String getEventId() {
        return eventId;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public AdEventType getType() {
        return type;
    }
}

package com.adflow.event;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AdEventService {

    private final AdEventRepository events;

    public AdEventService(AdEventRepository events) {
        this.events = events;
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
        return events.save(AdEvent.record(eventId, campaignId, creativeId, userId, contentId, type, occurredAt));
    }
}

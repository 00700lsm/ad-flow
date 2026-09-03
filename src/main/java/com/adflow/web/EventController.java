package com.adflow.web;

import com.adflow.event.AdEvent;
import com.adflow.event.AdEventService;
import com.adflow.event.AdEventType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class EventController {

    private final AdEventService events;

    public EventController(AdEventService events) {
        this.events = events;
    }

    @PostMapping("/events/impression")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse impression(@RequestBody EventRequest request) {
        return EventResponse.from(events.record(
                request.eventId(),
                request.campaignId(),
                request.creativeId(),
                request.userId(),
                request.contentId(),
                AdEventType.IMPRESSION,
                request.occurredAt()
        ));
    }

    @PostMapping("/events/click")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse click(@RequestBody EventRequest request) {
        return EventResponse.from(events.record(
                request.eventId(),
                request.campaignId(),
                request.creativeId(),
                request.userId(),
                request.contentId(),
                AdEventType.CLICK,
                request.occurredAt()
        ));
    }

    public record EventRequest(
            String eventId,
            Long campaignId,
            Long creativeId,
            Long userId,
            Long contentId,
            Instant occurredAt
    ) {
    }

    public record EventResponse(String eventId, String type) {
        static EventResponse from(AdEvent event) {
            return new EventResponse(event.getEventId(), event.getType().name());
        }
    }
}

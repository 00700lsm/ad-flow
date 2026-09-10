package com.adflow.event;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class AdEventService {

    private final AdEventRepository events;
    private final BlockingQueue<AdEvent> pending = new LinkedBlockingQueue<>();
    private final ExecutorService worker = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "ad-event-persist");
        thread.setDaemon(true);
        return thread;
    });
    private final long persistDelayMs;

    public AdEventService(
            AdEventRepository events,
            @Value("${adflow.event.persist-delay-ms:0}") long persistDelayMs
    ) {
        this.events = events;
        this.persistDelayMs = persistDelayMs;
    }

    @PostConstruct
    void startWorker() {
        worker.submit(this::drain);
    }

    @PreDestroy
    void stopWorker() {
        worker.shutdownNow();
    }

    public AdEvent accept(
            String eventId,
            Long campaignId,
            Long creativeId,
            Long userId,
            Long contentId,
            AdEventType type,
            Instant occurredAt
    ) {
        AdEvent event = AdEvent.record(eventId, campaignId, creativeId, userId, contentId, type, occurredAt);
        try {
            pending.put(event);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("이벤트 큐 적재가 중단되었습니다", e);
        }
        return event;
    }

    private void drain() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                AdEvent next = pending.take();
                delayIfConfigured();
                persistIgnoringDuplicate(next);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void persistIgnoringDuplicate(AdEvent next) {
        try {
            events.save(next);
        } catch (DataIntegrityViolationException ignored) {
            // UNIQUE(eventId) — 집계는 한 행만
        }
    }

    private void delayIfConfigured() throws InterruptedException {
        if (persistDelayMs > 0) {
            Thread.sleep(persistDelayMs);
        }
    }
}

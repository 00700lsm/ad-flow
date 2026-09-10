package com.adflow.simulation;

import com.adflow.ads.AdServingService;
import com.adflow.ads.SelectedAd;
import com.adflow.event.AdEventService;
import com.adflow.event.AdEventType;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SimulationService {

    static final long USER_TWENTIES = 1L;
    static final long USER_FORTIES = 2L;
    static final long CONTENT_SPORTS = 1L;
    static final long CONTENT_DRAMA = 2L;

    private final AdServingService ads;
    private final AdEventService events;
    private final AtomicLong ids = new AtomicLong();
    private final Map<Long, Simulation> simulations = new ConcurrentHashMap<>();

    public SimulationService(AdServingService ads, AdEventService events) {
        this.ads = ads;
        this.events = events;
    }

    public Simulation create(int concurrentUsers, Map<String, Integer> ageShares, List<String> categories, Integer clickRate) {
        if (concurrentUsers < 1) {
            throw new IllegalArgumentException("concurrentUsers는 1 이상이어야 합니다");
        }
        Simulation simulation = new Simulation(
                ids.incrementAndGet(),
                concurrentUsers,
                defaultAgeShares(ageShares),
                defaultCategories(categories),
                clickRate == null ? 0 : clickRate
        );
        simulations.put(simulation.getId(), simulation);
        return simulation;
    }

    public Simulation start(long id) {
        Simulation simulation = require(id);
        if (!simulation.beginStart()) {
            return simulation;
        }
        int n = simulation.getConcurrentUsers();
        List<String> ages = expandShares(simulation.getAgeShares(), n);
        List<String> cats = expandCategories(simulation.getCategories(), n);
        int count = 0;
        for (int i = 0; i < n; i++) {
            long userId = userId(ages.get(i));
            long contentId = contentId(cats.get(i));
            SelectedAd selected = ads.select(userId, contentId);
            accept(selected, userId, contentId, AdEventType.IMPRESSION);
            if (i < n * simulation.getClickRate() / 100) {
                accept(selected, userId, contentId, AdEventType.CLICK);
            }
            count++;
        }
        simulation.setRequestCount(count);
        return simulation;
    }

    public Simulation stop(long id) {
        Simulation simulation = require(id);
        simulation.stopIfCreated();
        return simulation;
    }

    private void accept(SelectedAd selected, long userId, long contentId, AdEventType type) {
        events.accept(
                UUID.randomUUID().toString(),
                selected.campaignId(),
                selected.creativeId(),
                userId,
                contentId,
                type,
                Instant.now()
        );
    }

    private Simulation require(long id) {
        Simulation simulation = simulations.get(id);
        if (simulation == null) {
            throw new IllegalArgumentException("시뮬레이션을 찾을 수 없습니다: " + id);
        }
        return simulation;
    }

    private static Map<String, Integer> defaultAgeShares(Map<String, Integer> ageShares) {
        if (ageShares == null || ageShares.isEmpty()) {
            LinkedHashMap<String, Integer> defaults = new LinkedHashMap<>();
            defaults.put("20대", 100);
            return defaults;
        }
        return new LinkedHashMap<>(ageShares);
    }

    private static List<String> defaultCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of("스포츠");
        }
        return new ArrayList<>(categories);
    }

    private static final List<String> AGE_ORDER = List.of("20대", "30대", "40대");

    static List<String> expandShares(Map<String, Integer> shares, int total) {
        List<String> keys = new ArrayList<>();
        for (String band : AGE_ORDER) {
            if (shares.containsKey(band)) {
                keys.add(band);
            }
        }
        for (String key : shares.keySet()) {
            if (!keys.contains(key)) {
                keys.add(key);
            }
        }
        List<String> slots = new ArrayList<>();
        int remaining = total;
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            int n = i == keys.size() - 1 ? remaining : total * shares.get(key) / 100;
            remaining -= n;
            for (int j = 0; j < n; j++) {
                slots.add(key);
            }
        }
        return slots;
    }

    static List<String> expandCategories(List<String> categories, int total) {
        if (categories.size() == 1) {
            return Collections.nCopies(total, categories.get(0));
        }
        List<String> slots = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            slots.add(categories.get(i % categories.size()));
        }
        return slots;
    }

    static long userId(String ageBand) {
        return "40대".equals(ageBand) ? USER_FORTIES : USER_TWENTIES;
    }

    static long contentId(String category) {
        return "드라마".equals(category) ? CONTENT_DRAMA : CONTENT_SPORTS;
    }
}

package com.adflow.simulation;

import com.adflow.ads.AdServingService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SimulationService {

    static final long SAMPLE_USER_ID = 1L;
    static final long SAMPLE_CONTENT_ID = 1L;

    private final AdServingService ads;
    private final AtomicLong ids = new AtomicLong();
    private final Map<Long, Simulation> simulations = new ConcurrentHashMap<>();

    public SimulationService(AdServingService ads) {
        this.ads = ads;
    }

    public Simulation create(int concurrentUsers) {
        if (concurrentUsers < 1) {
            throw new IllegalArgumentException("concurrentUsers는 1 이상이어야 합니다");
        }
        Simulation simulation = new Simulation(ids.incrementAndGet(), concurrentUsers);
        simulations.put(simulation.getId(), simulation);
        return simulation;
    }

    public Simulation start(long id) {
        Simulation simulation = require(id);
        if (!simulation.beginStart()) {
            return simulation;
        }
        int count = 0;
        for (int i = 0; i < simulation.getConcurrentUsers(); i++) {
            ads.select(SAMPLE_USER_ID, SAMPLE_CONTENT_ID);
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

    private Simulation require(long id) {
        Simulation simulation = simulations.get(id);
        if (simulation == null) {
            throw new IllegalArgumentException("시뮬레이션을 찾을 수 없습니다: " + id);
        }
        return simulation;
    }
}

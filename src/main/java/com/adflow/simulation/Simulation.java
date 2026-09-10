package com.adflow.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Simulation {

    public enum Status {
        CREATED,
        STOPPED,
        COMPLETED
    }

    private final long id;
    private final int concurrentUsers;
    private final Map<String, Integer> ageShares;
    private final List<String> categories;
    private Status status = Status.CREATED;
    private int requestCount;

    public Simulation(long id, int concurrentUsers, Map<String, Integer> ageShares, List<String> categories) {
        this.id = id;
        this.concurrentUsers = concurrentUsers;
        this.ageShares = Collections.unmodifiableMap(new LinkedHashMap<>(ageShares));
        this.categories = Collections.unmodifiableList(new ArrayList<>(categories));
    }

    public long getId() {
        return id;
    }

    public int getConcurrentUsers() {
        return concurrentUsers;
    }

    public Map<String, Integer> getAgeShares() {
        return ageShares;
    }

    public List<String> getCategories() {
        return categories;
    }

    public synchronized Status getStatus() {
        return status;
    }

    public synchronized int getRequestCount() {
        return requestCount;
    }

    public synchronized boolean stopIfCreated() {
        if (status != Status.CREATED) {
            return false;
        }
        status = Status.STOPPED;
        return true;
    }

    public synchronized boolean beginStart() {
        if (status != Status.CREATED) {
            return false;
        }
        status = Status.COMPLETED;
        return true;
    }

    public synchronized void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }
}

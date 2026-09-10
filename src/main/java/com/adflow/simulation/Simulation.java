package com.adflow.simulation;

public class Simulation {

    public enum Status {
        CREATED,
        STOPPED,
        COMPLETED
    }

    private final long id;
    private final int concurrentUsers;
    private Status status = Status.CREATED;
    private int requestCount;

    public Simulation(long id, int concurrentUsers) {
        this.id = id;
        this.concurrentUsers = concurrentUsers;
    }

    public long getId() {
        return id;
    }

    public int getConcurrentUsers() {
        return concurrentUsers;
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

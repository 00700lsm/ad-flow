package com.adflow.web;

import com.adflow.simulation.Simulation;
import com.adflow.simulation.SimulationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimulationController {

    private final SimulationService simulations;

    public SimulationController(SimulationService simulations) {
        this.simulations = simulations;
    }

    @PostMapping("/simulations")
    @ResponseStatus(HttpStatus.CREATED)
    public SimulationResponse create(@RequestBody CreateRequest request) {
        return SimulationResponse.from(simulations.create(request.concurrentUsers()));
    }

    @PostMapping("/simulations/{id}/start")
    public SimulationResponse start(@PathVariable long id) {
        return SimulationResponse.from(simulations.start(id));
    }

    @PostMapping("/simulations/{id}/stop")
    public SimulationResponse stop(@PathVariable long id) {
        return SimulationResponse.from(simulations.stop(id));
    }

    public record CreateRequest(int concurrentUsers) {
    }

    public record SimulationResponse(long id, int concurrentUsers, int requestCount, String status) {
        static SimulationResponse from(Simulation simulation) {
            return new SimulationResponse(
                    simulation.getId(),
                    simulation.getConcurrentUsers(),
                    simulation.getRequestCount(),
                    simulation.getStatus().name()
            );
        }
    }
}

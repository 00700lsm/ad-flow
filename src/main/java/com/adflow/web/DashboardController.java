package com.adflow.web;

import com.adflow.dashboard.CampaignStats;
import com.adflow.dashboard.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DashboardController {

    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/dashboard/campaigns/{campaignId}")
    public CampaignStats campaign(@PathVariable Long campaignId) {
        return dashboard.stats(campaignId);
    }

    @GetMapping("/dashboard/summary")
    public Map<String, List<CampaignStats>> summary() {
        return Map.of("campaigns", dashboard.summary());
    }
}

package com.adflow.web;

import com.adflow.ads.AdServingService;
import com.adflow.ads.SelectedAd;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdController {

    private final AdServingService ads;

    public AdController(AdServingService ads) {
        this.ads = ads;
    }

    @GetMapping("/ads")
    public SelectedAd get(@RequestParam Long userId, @RequestParam Long contentId) {
        return ads.select(userId, contentId);
    }
}

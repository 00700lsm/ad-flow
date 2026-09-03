package com.adflow.web;

import com.adflow.campaign.CampaignService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CampaignController {

    private final CampaignService campaigns;

    public CampaignController(CampaignService campaigns) {
        this.campaigns = campaigns;
    }

    @PostMapping("/campaigns")
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignDtos.CampaignResponse create(@RequestBody CampaignDtos.CreateRequest request) {
        return CampaignDtos.CampaignResponse.from(campaigns.create(
                request.name(),
                request.budget(),
                request.startAt(),
                request.endAt(),
                request.priority(),
                request.targetAgeMin(),
                request.targetAgeMax(),
                request.targetCategory(),
                request.frequencyCap()
        ));
    }

    @GetMapping("/campaigns")
    public List<CampaignDtos.CampaignResponse> list() {
        return campaigns.list().stream().map(CampaignDtos.CampaignResponse::from).toList();
    }

    @GetMapping("/campaigns/{id}")
    public CampaignDtos.CampaignResponse get(@PathVariable Long id) {
        return CampaignDtos.CampaignResponse.from(campaigns.get(id));
    }

    @PatchMapping("/campaigns/{id}")
    public CampaignDtos.CampaignResponse patch(@PathVariable Long id, @RequestBody CampaignDtos.PatchRequest request) {
        return CampaignDtos.CampaignResponse.from(campaigns.patch(
                id,
                request.name(),
                request.status(),
                request.budget(),
                request.startAt(),
                request.endAt(),
                request.priority(),
                request.targetAgeMin(),
                request.targetAgeMax(),
                request.targetCategory(),
                request.frequencyCap()
        ));
    }

    @PostMapping("/campaigns/{id}/creatives")
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignDtos.CreativeResponse addCreative(
            @PathVariable Long id,
            @RequestBody CampaignDtos.CreativeRequest request
    ) {
        return CampaignDtos.CreativeResponse.from(
                campaigns.addCreative(id, request.type(), request.mediaUrl(), request.clickUrl())
        );
    }
}

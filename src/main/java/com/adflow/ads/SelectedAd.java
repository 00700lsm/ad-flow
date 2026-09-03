package com.adflow.ads;

public record SelectedAd(
        Long campaignId,
        Long creativeId,
        String campaignName,
        String type,
        String mediaUrl,
        String clickUrl
) {
}

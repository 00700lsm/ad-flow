package com.adflow.creative;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "creatives")
public class Creative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long campaignId;
    private String type;
    private String mediaUrl;
    private String clickUrl;

    protected Creative() {
    }

    public static Creative attach(Long campaignId, String type, String mediaUrl, String clickUrl) {
        if (campaignId == null) {
            throw new IllegalArgumentException("campaignId는 필수입니다");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("소재 타입은 필수입니다");
        }
        if (mediaUrl == null || mediaUrl.isBlank()) {
            throw new IllegalArgumentException("mediaUrl은 필수입니다");
        }
        if (clickUrl == null || clickUrl.isBlank()) {
            throw new IllegalArgumentException("clickUrl은 필수입니다");
        }
        Creative creative = new Creative();
        creative.campaignId = campaignId;
        creative.type = type.trim();
        creative.mediaUrl = mediaUrl.trim();
        creative.clickUrl = clickUrl.trim();
        return creative;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public String getType() {
        return type;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getClickUrl() {
        return clickUrl;
    }
}

package com.adflow.ads;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class FrequencyCapCounter {

    private final FrequencyCapCountRepository counts;

    public FrequencyCapCounter(FrequencyCapCountRepository counts) {
        this.counts = counts;
    }

    @Transactional
    public boolean tryIncrement(Long userId, Long campaignId, LocalDate utcDay, int cap) {
        if (counts.incrementIfBelow(userId, campaignId, utcDay, cap) > 0) {
            return true;
        }
        if (counts.existsByUserIdAndCampaignIdAndUtcDay(userId, campaignId, utcDay)) {
            return false;
        }
        try {
            counts.saveAndFlush(FrequencyCapCount.initial(userId, campaignId, utcDay));
            return true;
        } catch (DataIntegrityViolationException ignored) {
            return counts.incrementIfBelow(userId, campaignId, utcDay, cap) > 0;
        }
    }
}

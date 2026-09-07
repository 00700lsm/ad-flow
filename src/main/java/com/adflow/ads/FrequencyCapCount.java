package com.adflow.ads;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

@Entity
@Table(
        name = "frequency_cap_counts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_freq_cap_user_campaign_day",
                columnNames = {"user_id", "campaign_id", "utc_day"}
        )
)
public class FrequencyCapCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(name = "utc_day", nullable = false)
    private LocalDate utcDay;

    @Column(name = "cap_count", nullable = false)
    private int count;

    protected FrequencyCapCount() {
    }

    public static FrequencyCapCount initial(Long userId, Long campaignId, LocalDate utcDay) {
        FrequencyCapCount row = new FrequencyCapCount();
        row.userId = userId;
        row.campaignId = campaignId;
        row.utcDay = utcDay;
        row.count = 1;
        return row;
    }
}

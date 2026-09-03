package com.adflow.campaign;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private CampaignStatus status;

    private long budget;
    private long spentBudget;
    private Instant startAt;
    private Instant endAt;
    private int priority;
    private int targetAgeMin;
    private int targetAgeMax;
    private String targetCategory;
    private int frequencyCap;

    protected Campaign() {
    }

    public static Campaign create(
            String name,
            long budget,
            Instant startAt,
            Instant endAt,
            int priority,
            int targetAgeMin,
            int targetAgeMax,
            String targetCategory,
            int frequencyCap
    ) {
        Campaign campaign = new Campaign();
        campaign.apply(
                name,
                budget,
                startAt,
                endAt,
                priority,
                targetAgeMin,
                targetAgeMax,
                targetCategory,
                frequencyCap
        );
        campaign.status = CampaignStatus.ACTIVE;
        campaign.spentBudget = 0L;
        return campaign;
    }

    public void update(
            String name,
            Long budget,
            Instant startAt,
            Instant endAt,
            Integer priority,
            Integer targetAgeMin,
            Integer targetAgeMax,
            String targetCategory,
            Integer frequencyCap
    ) {
        apply(
                name != null ? name : this.name,
                budget != null ? budget : this.budget,
                startAt != null ? startAt : this.startAt,
                endAt != null ? endAt : this.endAt,
                priority != null ? priority : this.priority,
                targetAgeMin != null ? targetAgeMin : this.targetAgeMin,
                targetAgeMax != null ? targetAgeMax : this.targetAgeMax,
                targetCategory != null ? targetCategory : this.targetCategory,
                frequencyCap != null ? frequencyCap : this.frequencyCap
        );
    }

    public void pause() {
        if (status != CampaignStatus.ACTIVE) {
            throw new IllegalStateException("ACTIVE 캠페인만 중지할 수 있습니다");
        }
        status = CampaignStatus.PAUSED;
    }

    public void activate() {
        if (status != CampaignStatus.PAUSED) {
            throw new IllegalStateException("PAUSED 캠페인만 활성화할 수 있습니다");
        }
        status = CampaignStatus.ACTIVE;
    }

    public boolean isActiveAt(Instant now) {
        return status == CampaignStatus.ACTIVE
                && !now.isBefore(startAt)
                && !now.isAfter(endAt);
    }

    public boolean matches(int age, String contentCategory) {
        return age >= targetAgeMin
                && age <= targetAgeMax
                && targetCategory.equals(contentCategory);
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public long getBudget() {
        return budget;
    }

    public long getSpentBudget() {
        return spentBudget;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public int getPriority() {
        return priority;
    }

    public int getTargetAgeMin() {
        return targetAgeMin;
    }

    public int getTargetAgeMax() {
        return targetAgeMax;
    }

    public String getTargetCategory() {
        return targetCategory;
    }

    public int getFrequencyCap() {
        return frequencyCap;
    }

    private void apply(
            String name,
            long budget,
            Instant startAt,
            Instant endAt,
            int priority,
            int targetAgeMin,
            int targetAgeMax,
            String targetCategory,
            int frequencyCap
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("캠페인 이름은 필수입니다");
        }
        if (budget < 0) {
            throw new IllegalArgumentException("예산은 0 이상이어야 합니다");
        }
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("종료 시각은 시작 시각보다 뒤여야 합니다");
        }
        if (targetAgeMin < 0 || targetAgeMax < targetAgeMin) {
            throw new IllegalArgumentException("연령 타겟이 올바르지 않습니다");
        }
        if (targetCategory == null || targetCategory.isBlank()) {
            throw new IllegalArgumentException("타겟 장르는 필수입니다");
        }
        if (frequencyCap < 0) {
            throw new IllegalArgumentException("Frequency Cap은 0 이상이어야 합니다");
        }
        this.name = name.trim();
        this.budget = budget;
        this.startAt = startAt;
        this.endAt = endAt;
        this.priority = priority;
        this.targetAgeMin = targetAgeMin;
        this.targetAgeMax = targetAgeMax;
        this.targetCategory = targetCategory.trim();
        this.frequencyCap = frequencyCap;
    }
}

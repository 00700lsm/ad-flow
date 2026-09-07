package com.adflow.ads;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface FrequencyCapCountRepository extends JpaRepository<FrequencyCapCount, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update FrequencyCapCount c
            set c.count = c.count + 1
            where c.userId = :userId
              and c.campaignId = :campaignId
              and c.utcDay = :utcDay
              and c.count < :cap
            """)
    int incrementIfBelow(
            @Param("userId") Long userId,
            @Param("campaignId") Long campaignId,
            @Param("utcDay") LocalDate utcDay,
            @Param("cap") int cap
    );

    boolean existsByUserIdAndCampaignIdAndUtcDay(Long userId, Long campaignId, LocalDate utcDay);
}

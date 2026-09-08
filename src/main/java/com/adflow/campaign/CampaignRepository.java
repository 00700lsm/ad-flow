package com.adflow.campaign;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update campaigns
            set spent_budget = spent_budget + 1,
                status = case when spent_budget + 1 >= budget then 'BUDGET_EXHAUSTED' else status end
            where id = :id and spent_budget < budget
            """, nativeQuery = true)
    int tryCharge(@Param("id") Long id);
}

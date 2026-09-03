package com.adflow.creative;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreativeRepository extends JpaRepository<Creative, Long> {
    List<Creative> findByCampaignId(Long campaignId);
}

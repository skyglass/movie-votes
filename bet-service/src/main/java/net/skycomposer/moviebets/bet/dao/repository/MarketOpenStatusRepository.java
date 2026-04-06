package net.skycomposer.moviebets.bet.dao.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.skycomposer.moviebets.bet.dao.entity.MarketOpenStatusEntity;

@Repository
public interface MarketOpenStatusRepository extends JpaRepository<MarketOpenStatusEntity, UUID> {

    Optional<MarketOpenStatusEntity> findByMarketId(UUID marketId);

    boolean existsByMarketId(UUID marketId);
}

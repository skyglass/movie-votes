package net.skycomposer.moviebets.market.dao.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.skycomposer.moviebets.market.dao.entity.MarketCheckEntity;

@Repository
public interface MarketCheckRepository extends JpaRepository<MarketCheckEntity, UUID> {

    Optional<MarketCheckEntity> findByCheckId(Integer checkId);

    boolean existsByCheckId(Integer checkId);

}

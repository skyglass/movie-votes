package net.skycomposer.moviebets.bet.dao.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.skycomposer.moviebets.bet.dao.entity.UserItemStatusEntity;
import net.skycomposer.moviebets.common.dto.item.ItemType;
import net.skycomposer.moviebets.common.dto.item.UserItemStatus;

@Repository
public interface UserItemStatusRepository extends JpaRepository<UserItemStatusEntity, UUID> {

    @Query("""
        SELECT i.itemId
        FROM UserItemStatusEntity i
        WHERE i.userId = :userId
          AND i.itemType = :itemType
          AND i.itemId IN :itemIds
    """)
    List<String> findExistingItemIds(
            @Param("userId") String userId,
            @Param("itemType") ItemType itemType,
            @Param("itemIds") List<String> itemIds
    );

    boolean existsByUserIdAndItemIdAndItemType(String userId, String itemId, ItemType itemType);

    Optional<UserItemStatusEntity> findByUserIdAndItemIdAndItemType(String userId, String itemId, ItemType itemType);

    @Query("""
        SELECT u FROM UserItemStatusEntity u
        WHERE u.status = :status
          AND NOT EXISTS (
              SELECT 1 FROM MarketOpenStatusEntity m
              WHERE m.status = 'OPENED'
                AND m.itemType = u.itemType
                AND (m.item1Id = u.itemId OR m.item2Id = u.itemId)
          )
    """)
    List<UserItemStatusEntity> findFirstMatch(@Param("status") UserItemStatus status, Pageable pageable);

    @Query("""
        SELECT u FROM UserItemStatusEntity u
        WHERE u.status = :status
          AND u.itemType = :itemType
          AND u.userId <> :excludedUserId
          AND u.itemId <> :excludedItemId
          AND NOT EXISTS (
              SELECT 1 FROM MarketOpenStatusEntity m
              WHERE m.status = 'OPENED'
                AND m.itemType = u.itemType
                AND (m.item1Id = u.itemId OR m.item2Id = u.itemId)
          )
          AND NOT EXISTS (
              SELECT 1 FROM UserItemStatusEntity u2
              WHERE u2.itemType = u.itemType
                AND u2.itemId = :excludedItemId
                AND u2.userId = u.userId
          )      
    """)
    List<UserItemStatusEntity> findSecondMatch(
            @Param("status") UserItemStatus status,
            @Param("itemType") ItemType itemType,
            @Param("excludedUserId") String excludedUserId,
            @Param("excludedItemId") String excludedItemId,
            Pageable pageable
    );

}

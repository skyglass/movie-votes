package net.skycomposer.moviebets.bet.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.bet.dao.entity.UserItemStatusEntity;
import net.skycomposer.moviebets.bet.dao.repository.UserItemStatusRepository;
import net.skycomposer.moviebets.common.dto.bet.commands.UserItemStatusRequest;
import net.skycomposer.moviebets.common.dto.item.ItemType;
import net.skycomposer.moviebets.common.dto.item.UserItemStatus;
import net.skycomposer.moviebets.common.dto.item.UserItemVoteStatus;
import net.skycomposer.moviebets.common.dto.item.UserItemVoteStatusList;

@Service
@RequiredArgsConstructor
public class UserItemStatusService {

    private final UserItemStatusRepository userItemStatusRepository;

    @Transactional
    public boolean placeOrUpdateVote(UserItemStatusRequest userItemStatusRequest) {
        UserItemStatusEntity userItemStatusEntity = findBy(
                userItemStatusRequest.getUserId(), userItemStatusRequest.getItemId(), userItemStatusRequest.getItemType());
        if (userItemStatusEntity == null) {
            createUserItemStatusEntity(userItemStatusRequest.getUserId(), userItemStatusRequest.getItemId(),
                    userItemStatusRequest.getItemName(), userItemStatusRequest.getItemType(), userItemStatusRequest.getUserItemStatus());
            return false;
        } else if (userItemStatusEntity.getStatus() != userItemStatusRequest.getUserItemStatus()) {
            userItemStatusEntity.setStatus(userItemStatusRequest.getUserItemStatus());
            userItemStatusRepository.save(userItemStatusEntity);
        }
        return true;
    }

    public UserItemVoteStatusList getMovieVoteStatuses(String userId, List<String> itemIds) {
        List<String> existingItemIds = userItemStatusRepository.findExistingItemIds(userId, ItemType.MOVIE, itemIds);

        return new UserItemVoteStatusList(itemIds.stream()
                .map(id -> new UserItemVoteStatus(id, existingItemIds.contains(id)))
                .toList()
        );
    }

    private UserItemStatusEntity findBy(String userId, String itemId, ItemType itemType){
        return userItemStatusRepository.findByUserIdAndItemIdAndItemType(userId, itemId, itemType).orElse(null);
    }

    private UserItemStatusEntity createUserItemStatusEntity (String userId, String itemId, String itemName,
                                                             ItemType itemType, UserItemStatus userItemStatus) {
        UserItemStatusEntity userItemStatusEntity = userItemStatusRepository.save(UserItemStatusEntity.builder()
                 .userId(userId)
                 .itemId(itemId)
                 .itemName(itemName)
                 .itemType(itemType)
                 .status(userItemStatus)
                 .build());
        return userItemStatusEntity;
    }

}

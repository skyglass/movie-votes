package net.skycomposer.moviebets.bet.service.application;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.skycomposer.moviebets.bet.dao.entity.UserItemStatusEntity;
import net.skycomposer.moviebets.bet.dao.repository.UserItemStatusRepository;
import net.skycomposer.moviebets.bet.exception.UserItemStatusRequestDeniedException;
import net.skycomposer.moviebets.common.dto.bet.UserItemStatusResponse;
import net.skycomposer.moviebets.common.dto.bet.commands.UserBetPairOpenMarketCommand;
import net.skycomposer.moviebets.common.dto.bet.commands.UserItemStatusRequest;
import net.skycomposer.moviebets.common.dto.item.UserItemStatus;
import net.skycomposer.moviebets.common.dto.market.commands.MarketOpenCheckCommand;

@Component
public class UserItemStatusApplicationService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final UserItemStatusRepository userItemStatusRepository;

    private final String userItemStatusTopicName;

    private final String marketCommandsTopicName;

    public UserItemStatusApplicationService(final UserItemStatusRepository userItemStatusRepository,
                                            final KafkaTemplate<String, Object> kafkaTemplate,
                                            final @Value("${user.item-status.topic.name}") String userItemStatusTopicName,
                                            final @Value("${market.commands.topic.name}") String marketCommandsTopicName) {
        this.userItemStatusRepository = userItemStatusRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.userItemStatusTopicName = userItemStatusTopicName;
        this.marketCommandsTopicName = marketCommandsTopicName;
    }

    @Transactional
    public UserItemStatusResponse placeVoteAsync(UserItemStatusRequest userItemStatusRequest, String authenticatedUserId) {
        if (!Objects.equals(userItemStatusRequest.getUserId(), authenticatedUserId)) {
            throw new UserItemStatusRequestDeniedException(authenticatedUserId, userItemStatusRequest.getUserId());
        }
        kafkaTemplate.send(userItemStatusTopicName, userItemStatusRequest.getUserId(), userItemStatusRequest);
        return UserItemStatusResponse.builder()
                .itemId(userItemStatusRequest.getItemId())
                .userId(userItemStatusRequest.getUserId())
                .message("Vote request has been sent for userId = %s and itemId = %s".formatted(userItemStatusRequest.getUserId(), userItemStatusRequest.getItemId()))
                .build();
    }

    @Transactional
    public void openMarket(MarketOpenCheckCommand marketOpenCheckCommand) {
        List<UserItemStatusEntity> firstUserItemStatusList = userItemStatusRepository.findFirstMatch(UserItemStatus.VOTED, PageRequest.of(0, 1));
        if (firstUserItemStatusList.isEmpty()) {
            return;
        }
        UserItemStatusEntity firstUserItemStatus = firstUserItemStatusList.getFirst();
        List<UserItemStatusEntity> secondUserItemStatusList = userItemStatusRepository
                .findSecondMatch(
                        UserItemStatus.VOTED,
                        firstUserItemStatus.getItemType(),
                        firstUserItemStatus.getUserId(),
                        firstUserItemStatus.getItemId(),
                        PageRequest.of(0, 1));
        if (secondUserItemStatusList.isEmpty()) {
            return;
        }
        UserItemStatusEntity secondUserItemStatus = secondUserItemStatusList.getFirst();
        firstUserItemStatus.setStatus(UserItemStatus.BET_PLACED);
        userItemStatusRepository.save(firstUserItemStatus);
        secondUserItemStatus.setStatus(UserItemStatus.BET_PLACED);
        userItemStatusRepository.save(secondUserItemStatus);
        UserBetPairOpenMarketCommand userBetPairOpenMarketCommand = UserBetPairOpenMarketCommand.builder()
                .user1Id(firstUserItemStatus.getUserId())
                .item1Id(firstUserItemStatus.getItemId())
                .item1Name(firstUserItemStatus.getItemName())
                .itemType(firstUserItemStatus.getItemType())
                .user2Id(secondUserItemStatus.getUserId())
                .item2Id(secondUserItemStatus.getItemId())
                .item2Name((secondUserItemStatus.getItemName()))
                .build();
        //The key doesn't matter for the new market: sequential processing is not necessary
        kafkaTemplate.send(marketCommandsTopicName, firstUserItemStatus.getId().toString(), userBetPairOpenMarketCommand);
    }

}

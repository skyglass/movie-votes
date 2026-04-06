package net.skycomposer.moviebets.bet.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.skycomposer.moviebets.bet.dao.entity.BetEntity;
import net.skycomposer.moviebets.bet.dao.entity.BetSettleRequestEntity;
import net.skycomposer.moviebets.bet.dao.entity.MarketOpenStatusEntity;
import net.skycomposer.moviebets.bet.dao.entity.MarketSettleStatusEntity;
import net.skycomposer.moviebets.bet.dao.repository.BetRepository;
import net.skycomposer.moviebets.bet.dao.repository.BetSettleRequestRepository;
import net.skycomposer.moviebets.bet.dao.repository.MarketOpenStatusRepository;
import net.skycomposer.moviebets.bet.dao.repository.MarketSettleStatusRepository;
import net.skycomposer.moviebets.bet.exception.*;
import net.skycomposer.moviebets.common.dto.bet.*;
import net.skycomposer.moviebets.common.dto.bet.commands.UserItemStatusRequest;
import net.skycomposer.moviebets.common.dto.bet.events.BetCreatedEvent;
import net.skycomposer.moviebets.common.dto.item.UserItemStatus;
import net.skycomposer.moviebets.common.dto.market.MarketOpenStatus;
import net.skycomposer.moviebets.common.dto.market.MarketResult;
import net.skycomposer.moviebets.common.dto.market.events.MarketOpenedEvent;

@Service
public class BetServiceImpl implements BetService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BetRepository betRepository;

    private final MarketSettleStatusRepository marketSettleStatusRepository;

    private final MarketOpenStatusRepository marketOpenStatusRepository;

    private final BetSettleRequestRepository betSettleRequestRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String betCommandsTopicName;

    private final String userItemStatusTopicName;


    public BetServiceImpl(
            final BetRepository betRepository,
            final MarketSettleStatusRepository marketSettleStatusRepository,
            final MarketOpenStatusRepository marketOpenStatusRepository,
            final BetSettleRequestRepository betSettleRequestRepository,
            final KafkaTemplate<String, Object> kafkaTemplate,
            final @Value("${bet.commands.topic.name}") String betCommandsTopicName,
            final @Value("${user.item-status.topic.name}") String userItemStatusTopicName
    ) {
        this.betRepository = betRepository;
        this.marketSettleStatusRepository = marketSettleStatusRepository;
        this.marketOpenStatusRepository = marketOpenStatusRepository;
        this.betSettleRequestRepository = betSettleRequestRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.betCommandsTopicName = betCommandsTopicName;
        this.userItemStatusTopicName = userItemStatusTopicName;
    }


    @Override
    @Transactional(readOnly = true)
    public BetData findBetById(UUID betId) {
        BetEntity betEntity = betRepository.findById(betId).get();
        if (betEntity == null) {
            throw new BetNotFoundException(betId);
        }
        return createBetData(betEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BetData> findAll() {
        return betRepository.findAll().stream()
                .map(entity -> createBetData(entity))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BetResponse placeBet(BetData betData, String authenticatedCustomerId) {
        if (!Objects.equals(betData.getCustomerId(), authenticatedCustomerId)) {
            throw new BetOpenDeniedException(authenticatedCustomerId, betData.getCustomerId());
        }
        if (isMarketClosed(betData.getMarketId())) {
            throw new MarketIsClosedException(getMarketName(betData));
        }
        if (betRepository.existsByCustomerIdAndMarketId(betData.getCustomerId(), betData.getMarketId())) {
            throw new BetAlreadyExistsException(betData.getCustomerId(), getMarketName(betData));
        }
        BetEntity betEntity = createBetEntity(betData);
        betEntity.setStatus(BetStatus.PlACED);
        betEntity = betRepository.save(betEntity);
        BetCreatedEvent betCreatedEvent = createBetCreatedEvent(betEntity, betData, betData.getRequestId(), betData.getCancelRequestId());
        kafkaTemplate.send(betCommandsTopicName, betEntity.getId().toString(), betCreatedEvent);
        kafkaTemplate.send(userItemStatusTopicName, authenticatedCustomerId, createUserItemStatusRequest(betData));
        return new BetResponse(betEntity.getId(),
                "Bet %s created successfully".formatted(betEntity.getId()));
    }

    @Override
    @Transactional
    public BetResponse cancel(CancelBetRequest cancelBetRequest, String authenticatedCustomerId, boolean isAdmin) {
        BetEntity betEntity = betRepository.findById(cancelBetRequest.getBetId()).get();
        if (betEntity == null) {
            throw new BetNotFoundException(cancelBetRequest.getBetId());
        }
        if (!isAdmin && !Objects.equals(betEntity.getCustomerId(), authenticatedCustomerId)) {
            throw new BetCloseDeniedException(authenticatedCustomerId, betEntity.getCustomerId());
        }
        if (isMarketClosed(betEntity.getMarketId())) {
            throw new MarketIsClosedException(betEntity.getMarketId().toString());
        }
        betEntity.setStatus(BetStatus.CANCELLED);
        betEntity = betRepository.save(betEntity);
        return new BetResponse(betEntity.getId(),
                "Bet %s cancelled successfully".formatted(betEntity.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public BetData getState(UUID betId) {
        return findBetById(betId);
    }

    @Override
    @Transactional(readOnly = true)
    public SumStakesData getBetsByMarket(UUID marketId) {
        List<SumStakeData> groupedStakes = betRepository.findStakeSumGroupedByResult(marketId);
        return SumStakesData.builder()
                .sumStakes(groupedStakes)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BetDataList getBetsForMarket(UUID marketId, boolean skipMarketOpenCheck) {
        if (!skipMarketOpenCheck && !isMarketClosed(marketId)) {
            throw new MarketIsOpenException(marketId);
        }
        List<BetEntity> betEntityList = betRepository.findByMarketId(marketId);
        return BetDataList.builder()
                .betDataList(createBetDataList(betEntityList))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BetDataList getBetsForPlayer(String customerId) {
        List<BetEntity> betEntityList = betRepository.findByCustomerId(customerId);
        return BetDataList.builder()
                .betDataList(createBetDataList(betEntityList))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BetData> findByMarketAndStatus(UUID marketId, BetStatus betStatus, Integer limit) {
        return betRepository.findByMarketIdAndStatus(marketId, betStatus, PageRequest.of(0, limit))
                .stream()
                .map(e -> createBetData(e))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStatus(List<UUID> betUuids, BetStatus betStatus) {
        if (betUuids == null || betUuids.isEmpty()) {
            return;
        }
        betRepository.updateStatus(betUuids, betStatus);
    }

    @Override
    @Transactional
    public void setBetValidated(UUID betId) {
        updateStatus(List.of(betId), BetStatus.VALIDATED);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMarketClosed(UUID marketId) {
        return marketSettleStatusRepository.existsByMarketId(marketId);
    }

    @Override
    @Transactional(readOnly = true)
    public int countByMarketIdAndStatus(UUID marketId, BetStatus betStatus) {
        return betRepository.countByMarketIdAndStatus(marketId, betStatus).intValue();
    }

    @Override
    @Transactional(readOnly = true)
    public int countSettledBets(UUID marketId) {
        MarketSettleStatusEntity marketSettleStatusEntity = marketSettleStatusRepository.findByMarketId(marketId)
                .orElseThrow(() -> new IllegalArgumentException("Wrong usage of countSettledBets method"));
        return marketSettleStatusEntity.getFinishedCount();
    }

    @Override
    @Transactional(readOnly = true)
    public MarketStatusData getMarketStatus(UUID marketId, String customerId) {
        boolean marketClosed = isMarketClosed(marketId);
        boolean customerBetExists = betRepository.existsByCustomerIdAndMarketId(customerId, marketId);
        Integer votes = betRepository.countByMarketIdAndStatusIn(marketId, BetStatus.getValidStatuses());
        boolean canPlaceBet = !marketClosed && !customerBetExists;
        return MarketStatusData.builder()
                .canPlaceBet(canPlaceBet)
                .marketClosed(marketClosed)
                .votes(votes)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BetStatusResponse getBetStatus(String customerId, UUID marketId) {
        boolean betExists = betRepository.existsByCustomerIdAndMarketId(customerId, marketId);
        UUID betId = betExists ? betRepository.findByCustomerIdAndMarketId(customerId, marketId).get().getId() : null;
        return new BetStatusResponse(betId, betExists);
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceBetDataList getOpenMarkets(String userId) {
        return new PlaceBetDataList(betRepository.findOpenMarketsExcludingWhereUserAlreadyPlacedBet(userId));
    }

    @Override
    @Transactional
    public void marketSettleStart(UUID marketId, int expectedCount) {
        if (!marketSettleStatusRepository.existsByMarketId(marketId)) {
            MarketSettleStatusEntity marketSettleStatusEntity = MarketSettleStatusEntity.builder()
                .marketId(marketId)
                .expectedCount(expectedCount)
                .finishedCount(0)
                .build();
            marketSettleStatusRepository.save(marketSettleStatusEntity);
        }
    }

    @Override
    @Transactional
    public void updateMarketSettleCount(UUID betId, UUID marketId) {
        if (betSettleRequestRepository.existsByRequestId(betId)) {
            String message = String.format("Duplicate bet settle request for bet %s, market = %s", betId, marketId);
            logger.warn(message);
        } else {
            MarketSettleStatusEntity marketSettleStatusEntity = marketSettleStatusRepository.findByMarketId(marketId).get();
            marketSettleStatusEntity.setFinishedCount(marketSettleStatusEntity.getFinishedCount() + 1);
            marketSettleStatusRepository.save(marketSettleStatusEntity);
            betSettleRequestRepository.save(
                    BetSettleRequestEntity.builder()
                    .requestId(betId)
                    .marketId(marketId)
                    .build());
        }
    }

    @Override
    @Transactional
    public void marketSettleDone(UUID marketId, MarketResult winResult) {
        betSettleRequestRepository.deleteByMarketId(marketId);
        betRepository.settleBets(marketId, BetStatus.SETTLE_STARTED, BetStatus.SETTLED, winResult);
    }

    @Override
    @Transactional
    public void updateMarketOpenStatus(UUID marketId, MarketOpenStatus status) {
        MarketOpenStatusEntity marketOpenStatusEntity = marketOpenStatusRepository.findByMarketId(marketId)
                .orElseThrow(() -> new MarketOpenStatusNotFoundException(marketId));
        marketOpenStatusEntity.setStatus(status);
        marketOpenStatusRepository.save(marketOpenStatusEntity);
    }

    @Override
    @Transactional
    public void updateMarketOpenStatus(MarketOpenedEvent marketOpenedEvent) {
        if (!marketOpenStatusRepository.existsByMarketId(marketOpenedEvent.getMarketId())) {
            MarketOpenStatusEntity marketOpenStatusEntity = MarketOpenStatusEntity.builder()
                    .marketId(marketOpenedEvent.getMarketId())
                    .item1Id(marketOpenedEvent.getItem1Id())
                    .item2Id(marketOpenedEvent.getItem2Id())
                    .itemType(marketOpenedEvent.getItemType())
                    .status(MarketOpenStatus.OPENED)
                    .build();
            marketOpenStatusRepository.save(marketOpenStatusEntity);
        }
    }

    private BetData createBetData(BetEntity betEntity) {
        return BetData.builder()
                .betId(betEntity.getId())
                .customerId(betEntity.getCustomerId())
                .marketId(betEntity.getMarketId())
                .item1Id(betEntity.getItem1Id())
                .item1Name(betEntity.getItem1Name())
                .item2Id(betEntity.getItem2Id())
                .item2Name(betEntity.getItem2Name())
                .itemType(betEntity.getItemType())
                .stake(betEntity.getStake())
                .result(betEntity.getResult())
                .status(betEntity.getStatus())
                .betWon(betEntity.getBetWon())
                .build();
    }

    private List<BetData> createBetDataList(List<BetEntity> betEntityList) {
        return betEntityList.stream().map(entity -> createBetData(entity)).toList();
    }

    private BetEntity createBetEntity(BetData betData) {
        return BetEntity.builder()
                .customerId(betData.getCustomerId())
                .marketId(betData.getMarketId())
                .item1Id(betData.getItem1Id())
                .item1Name(betData.getItem1Name())
                .item2Id(betData.getItem2Id())
                .item2Name(betData.getItem2Name())
                .itemType(betData.getItemType())
                .stake(betData.getStake() == null ? 1 : betData.getStake())
                .result(betData.getResult())
                .build();
    }

    private BetCreatedEvent createBetCreatedEvent(BetEntity betEntity, BetData betData, UUID requestId, UUID cancelRequestId) {
        return BetCreatedEvent.builder()
                .betId(betEntity.getId())
                .customerId(betEntity.getCustomerId())
                .requestId(requestId)
                .cancelRequestId(cancelRequestId)
                .marketId(betEntity.getMarketId())
                .marketName(getMarketName(betData))
                .stake(betEntity.getStake())
                .result(betEntity.getResult())
                .build();
    }

    private UserItemStatusRequest createUserItemStatusRequest(BetData betData) {
        return UserItemStatusRequest.builder()
                .itemId(betData.getResult() == MarketResult.ITEM1_WINS ? betData.getItem1Id() : betData.getItem2Id())
                .itemName(betData.getResult() == MarketResult.ITEM1_WINS ? betData.getItem1Name() : betData.getItem2Name())
                .userId(betData.getCustomerId())
                .itemType(betData.getItemType())
                .userItemStatus(UserItemStatus.BET_PLACED)
                .build();
    }

    private String getMarketName(BetData betData) {
        return getMarketName(betData.getItem1Name(), betData.getItem2Name());
    }

    private String getMarketName(String item1Name, String item2Name) {
        return "['%s'] vs ['%s']".formatted(item1Name, item2Name);
    }


}

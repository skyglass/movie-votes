package net.skycomposer.moviebets.market.service;

import static java.time.Instant.now;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.skycomposer.moviebets.common.dto.bet.commands.UserBetPairOpenMarketCommand;
import net.skycomposer.moviebets.common.dto.bet.events.UserBetPairMarketOpenedEvent;
import net.skycomposer.moviebets.common.dto.market.*;
import net.skycomposer.moviebets.common.dto.market.commands.CloseMarketCommand;
import net.skycomposer.moviebets.common.dto.market.events.MarketCancelledEvent;
import net.skycomposer.moviebets.common.dto.market.events.MarketOpenedEvent;
import net.skycomposer.moviebets.market.dao.entity.MarketEntity;
import net.skycomposer.moviebets.market.dao.repository.MarketRepository;
import net.skycomposer.moviebets.market.exception.MarketNotFoundException;

@Service
public class MarketServiceImpl implements MarketService {

    private static final String FUNDS_ADDED_SUCCESSFULLY = "Funds successfully increased from %.4f to %.4f";

    private static final String FUNDS_REMOVED_SUCCESSFULLY = "Funds successfully decreased from %.4f to %.4f";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MarketRepository marketRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String betCommandsTopicName;

    private final String betSettleTopicName;

    private final Integer marketCloseTimeExtendMinutes;

    private final Integer marketCloseTimeDefaultMinutes;

    public MarketServiceImpl(
            MarketRepository marketRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${bet.commands.topic.name}") String betCommandsTopicName,
            @Value("${bet.settle.topic.name}") String betSettleTopicName,
            @Value("${market.check.close-time.extend-minutes}") Integer marketCloseTimeExtendMinutes,
            @Value("${market.check.close-time.default-minutes}") Integer marketCloseTimeDefaultMinutes
    ) {
        this.marketRepository = marketRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.betCommandsTopicName = betCommandsTopicName;
        this.betSettleTopicName = betSettleTopicName;
        this.marketCloseTimeExtendMinutes = marketCloseTimeExtendMinutes;
        this.marketCloseTimeDefaultMinutes = marketCloseTimeDefaultMinutes;
    }


    @Override
    @Transactional(readOnly = true)
    public MarketData findMarketById(UUID marketId) {
        MarketEntity marketEntity = marketRepository.findById(marketId).get();
        if (marketEntity == null) {
            throw new MarketNotFoundException(marketId);
        }
        return createMarketData(marketEntity);
    }

    @Override
    @Transactional
    public MarketResponse open(MarketData market) {
        MarketEntity marketEntity = new MarketEntity();
        marketEntity.setItem1Id(market.getItem1Id());
        marketEntity.setItem1Name(market.getItem1Name());
        marketEntity.setItem2Id(market.getItem2Id());
        marketEntity.setItem2Name(market.getItem2Name());
        marketEntity.setItemType(market.getItemType());
        marketEntity.setStatus(market.getStatus());
        marketEntity.setClosesAt(market.getClosesAt());
        marketEntity.setStatus(MarketStatus.OPENED);
        marketEntity = marketRepository.save(marketEntity);
        MarketOpenedEvent marketOpenedEvent = MarketOpenedEvent.builder()
                .marketId(marketEntity.getId())
                .item1Id(marketEntity.getItem1Id())
                .item2Id(marketEntity.getItem2Id())
                .itemType(marketEntity.getItemType())
                .build();
        kafkaTemplate.send(betSettleTopicName, marketEntity.getId().toString(), marketOpenedEvent);
        return new MarketResponse(marketEntity.getId(),
                "Market %s opened successfully".formatted(marketEntity.getId()));
    }

    @Override
    @Transactional
    public MarketResponse open(UserBetPairOpenMarketCommand request) {
        Instant closesAt = now().plus(Duration.ofMinutes(marketCloseTimeDefaultMinutes));
        MarketEntity marketEntity = new MarketEntity();
        marketEntity.setItem1Id(request.getItem1Id());
        marketEntity.setItem1Name(request.getItem1Name());
        marketEntity.setItem2Id(request.getItem2Id());
        marketEntity.setItem2Name(request.getItem2Name());
        marketEntity.setItemType(request.getItemType());
        marketEntity.setStatus(MarketStatus.OPENED);
        marketEntity.setClosesAt(closesAt);
        marketEntity = marketRepository.save(marketEntity);

        UserBetPairMarketOpenedEvent userBetPairMarketOpenedEvent = UserBetPairMarketOpenedEvent.builder()
                .user1Id(request.getUser1Id())
                .user2Id(request.getUser2Id())
                .item1Id(request.getItem1Id())
                .item2Id(request.getItem2Id())
                .item1Name(request.getItem1Name())
                .item2Name(request.getItem2Name())
                .itemType(request.getItemType())
                .marketId(marketEntity.getId())
                .build();
        kafkaTemplate.send(betCommandsTopicName, marketEntity.getId().toString(), userBetPairMarketOpenedEvent);
        MarketOpenedEvent marketOpenedEvent = MarketOpenedEvent.builder()
                .marketId(marketEntity.getId())
                .item1Id(userBetPairMarketOpenedEvent.getItem1Id())
                .item2Id(userBetPairMarketOpenedEvent.getItem2Id())
                .itemType(userBetPairMarketOpenedEvent.getItemType())
                .build();
        kafkaTemplate.send(betSettleTopicName, marketEntity.getId().toString(), marketOpenedEvent);
        return new MarketResponse(marketEntity.getId(),
                "Market %s opened successfully".formatted(marketEntity.getId()));
    }

    @Override
    @Transactional
    public MarketResponse close(UUID marketId) {
        CloseMarketCommand command = new CloseMarketCommand(marketId);
        kafkaTemplate.send(betSettleTopicName, marketId.toString(), command);
        return new MarketResponse(marketId,
                "Request to close Market %s has been sent successfully".formatted(marketId));
    }

    @Override
    @Transactional
    public MarketResponse marketCloseConfirmed(UUID marketId, MarketResult marketResult) {
        MarketEntity marketEntity = marketRepository.findById(marketId)
                .orElseThrow(() -> new MarketNotFoundException(marketId));
        marketEntity.setOpen(false);
        marketEntity.setStatus(MarketStatus.CLOSED);
        marketEntity.setResult(marketResult);
        marketRepository.save(marketEntity);
        return new MarketResponse(marketEntity.getId(),
                "Market %s closed successfully".formatted(marketEntity.getId()));
    }

    @Override
    @Transactional
    public MarketResponse marketCloseFailed(UUID marketId) {
        MarketEntity marketEntity = marketRepository.findById(marketId)
                .orElseThrow(() -> new MarketNotFoundException(marketId));
        Instant newClosesAt = now().plus(Duration.ofMinutes(marketCloseTimeExtendMinutes));
        marketEntity.setClosesAt(newClosesAt);
        marketRepository.save(marketEntity);
        return new MarketResponse(marketId,
                "Market close for market %s failed, because there is no winner yet: market close time was increased to continue".formatted(marketId));
    }

    @Override
    @Transactional
    public MarketResponse close(CloseMarketRequest request) {
        CloseMarketCommand command = new CloseMarketCommand(request.getMarketId());
        kafkaTemplate.send(betSettleTopicName, request.getMarketId().toString(), command);
        return new MarketResponse(request.getMarketId(),
                "Request to close Market %s has been sent successfully".formatted(request.getMarketId()));
    }

    @Override
    @Transactional
    public MarketResponse settle(UUID marketId) {
        MarketEntity marketEntity = marketRepository.findById(marketId).orElseThrow(
                () -> new MarketNotFoundException(marketId));
        marketEntity.setStatus(MarketStatus.SETTLED);
        marketEntity = marketRepository.save(marketEntity);
        return new MarketResponse(marketEntity.getId(),
                "Market %s settled successfully".formatted(marketEntity.getId()));
    }

    @Override
    @Transactional
    public MarketResponse cancel(CancelMarketRequest request) {
        var marketId = request.getMarketId();
        MarketEntity marketEntity = marketRepository.findById(marketId).orElseThrow(
                () -> new MarketNotFoundException(marketId));
        if (marketEntity.getStatus() == MarketStatus.OPENED) {
            marketEntity.setOpen(false);
            marketEntity.setStatus(MarketStatus.CANCELLED);
            marketRepository.save(marketEntity);
            MarketCancelledEvent marketCancelledEvent = new MarketCancelledEvent(marketId);
            kafkaTemplate.send(betSettleTopicName, marketId.toString(), marketCancelledEvent);
        }
        return new MarketResponse(marketId,
                "Market %s cancelled successfully, reason: %s".formatted(marketId, request.getReason()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarketData> findAll() {
        return marketRepository.findAllByOrderByClosesAtDesc().stream()
                .map(entity -> createMarketData(entity))
                .collect(Collectors.toList());
    }

    private MarketData createMarketData(MarketEntity marketEntity) {
        return MarketData.builder()
                .item1Id(marketEntity.getItem1Id())
                .item1Name(marketEntity.getItem1Name())
                .item2Id(marketEntity.getItem2Id())
                .item2Name(marketEntity.getItem2Name())
                .itemType(marketEntity.getItemType())
                .status(marketEntity.getStatus())
                .result(marketEntity.getResult())
                .open(marketEntity.getOpen())
                .closesAt(marketEntity.getClosesAt())
                .build();
    }

}

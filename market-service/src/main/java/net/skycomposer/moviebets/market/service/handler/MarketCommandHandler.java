package net.skycomposer.moviebets.market.service.handler;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.skycomposer.moviebets.common.dto.bet.commands.UserBetPairOpenMarketCommand;
import net.skycomposer.moviebets.common.dto.bet.commands.UserVotesUpdateCommand;
import net.skycomposer.moviebets.common.dto.market.MarketData;
import net.skycomposer.moviebets.common.dto.market.MarketResult;
import net.skycomposer.moviebets.common.dto.market.commands.SettleMarketCommand;
import net.skycomposer.moviebets.common.dto.market.events.MarketCloseConfirmedEvent;
import net.skycomposer.moviebets.common.dto.market.events.MarketCloseFailedEvent;
import net.skycomposer.moviebets.common.dto.market.events.MarketSettledEvent;
import net.skycomposer.moviebets.market.service.MarketService;

@Component
@KafkaListener(topics = "${market.commands.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
public class MarketCommandHandler {

    public static final String USER_VOTES_SINGLE_KEY_ID = UUID.randomUUID().toString();

    private final MarketService marketService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String betSettleTopicName;

    private final String userVotesTopicName;

    public MarketCommandHandler(
            MarketService marketService,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${bet.settle.topic.name}") String betSettleTopicName,
            @Value("${user.votes.topic.name}") String userVotesTopicName) {
        this.marketService = marketService;
        this.kafkaTemplate = kafkaTemplate;
        this.betSettleTopicName = betSettleTopicName;
        this.userVotesTopicName = userVotesTopicName;
    }

    @KafkaHandler
    public void handleUserBetPairOpenMarketCommand(@Payload UserBetPairOpenMarketCommand userBetPairOpenMarketCommand) {
        marketService.open(userBetPairOpenMarketCommand);
    }

    @KafkaHandler
    @Transactional
    public void handleSettleMarketCommand(@Payload SettleMarketCommand settleMarketCommand) {
        UUID marketId = settleMarketCommand.getMarketId();
        marketService.settle(marketId);

        MarketData marketData = marketService.findMarketById(marketId);
        MarketResult marketResult = marketData.getResult();
        UserVotesUpdateCommand userVotesUpdateCommand = UserVotesUpdateCommand.builder()
                .itemType(marketData.getItemType())
                .itemWon(marketResult == MarketResult.ITEM1_WINS ? marketData.getItem1Id() : marketData.getItem2Id())
                .itemLost(marketResult == MarketResult.ITEM1_WINS ? marketData.getItem2Id() : marketData.getItem1Id())
                .item1Won(marketResult == MarketResult.ITEM1_WINS)
                .marketId(marketId)
                .build();
        kafkaTemplate.send(userVotesTopicName, USER_VOTES_SINGLE_KEY_ID, userVotesUpdateCommand);

        MarketSettledEvent marketSettledEvent = new MarketSettledEvent(marketId, settleMarketCommand.getWinResult());
        kafkaTemplate.send(betSettleTopicName, marketId.toString(), marketSettledEvent);
    }

    @KafkaHandler
    @Transactional
    public void handleMarketCloseConfirmedEvent(@Payload MarketCloseConfirmedEvent marketCloseConfirmedEvent) {
        UUID marketId = marketCloseConfirmedEvent.getMarketId();
        MarketResult marketResult = marketCloseConfirmedEvent.getMarketResult();
        marketService.marketCloseConfirmed(marketId, marketResult);
    }

    @KafkaHandler
    public void handleMarketCloseFailedEvent(@Payload MarketCloseFailedEvent marketCloseFailedEvent) {
        UUID marketId = marketCloseFailedEvent.getMarketId();
        marketService.marketCloseFailed(marketId);
    }


}

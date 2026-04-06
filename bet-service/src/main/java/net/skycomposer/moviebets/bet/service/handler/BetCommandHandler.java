package net.skycomposer.moviebets.bet.service.handler;

import java.math.BigDecimal;
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

import net.skycomposer.moviebets.bet.service.BetService;
import net.skycomposer.moviebets.bet.service.application.UserItemStatusApplicationService;
import net.skycomposer.moviebets.common.dto.bet.BetData;
import net.skycomposer.moviebets.common.dto.bet.CancelBetRequest;
import net.skycomposer.moviebets.common.dto.bet.commands.RejectBetCommand;
import net.skycomposer.moviebets.common.dto.bet.commands.SettleBetCommand;
import net.skycomposer.moviebets.common.dto.bet.events.BetCreatedEvent;
import net.skycomposer.moviebets.common.dto.bet.events.BetSettledEvent;
import net.skycomposer.moviebets.common.dto.bet.events.UserBetPairMarketOpenedEvent;
import net.skycomposer.moviebets.common.dto.customer.commands.ReserveFundsCommand;
import net.skycomposer.moviebets.common.dto.customer.commands.SettleFundsCommand;
import net.skycomposer.moviebets.common.dto.market.MarketResult;
import net.skycomposer.moviebets.common.dto.market.commands.MarketOpenCheckCommand;

@Component
@KafkaListener(topics = "${bet.commands.topic.name}", groupId = "${spring.kafka.consumer.bet-commands.group-id}")
public class BetCommandHandler {

    private final BetService betService;

    private final UserItemStatusApplicationService userItemStatusApplicationService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String betSettleJobTopicName;

    private final String customerCommandsTopicName;

    private final String betCommandsTopicName;

    public BetCommandHandler(BetService betService,
            UserItemStatusApplicationService userItemStatusApplicationService,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${bet.settle-job.topic.name}") String betSettleJobTopicName,
            @Value("${customer.commands.topic.name}") String customerCommandsTopicName,
            @Value("${bet.commands.topic.name}") String betCommandsTopicName) {
        this.betService = betService;
        this.userItemStatusApplicationService = userItemStatusApplicationService;
        this.kafkaTemplate = kafkaTemplate;
        this.betSettleJobTopicName = betSettleJobTopicName;
        this.customerCommandsTopicName = customerCommandsTopicName;
        this.betCommandsTopicName = betCommandsTopicName;
    }

    @KafkaHandler
    public void handleCommand(@Payload RejectBetCommand rejectBetCommand) {
        CancelBetRequest cancelBetRequest = new CancelBetRequest(rejectBetCommand.getBetId(), rejectBetCommand.getReason());
        betService.cancel(cancelBetRequest, rejectBetCommand.getCustomerId(), false);
    }

    @KafkaHandler
    @Transactional
    public void handleCommand(@Payload SettleBetCommand settleBetCommand) {
        if (settleBetCommand.isWinner()) {
            SettleFundsCommand settleFundsCommand = new SettleFundsCommand(
                    settleBetCommand.getBetId(),
                    settleBetCommand.getCustomerId(),
                    settleBetCommand.getMarketId(),
                    settleBetCommand.getRequestId(),
                    settleBetCommand.getWinnerEarned().add(new BigDecimal(settleBetCommand.getStake())));
            kafkaTemplate.send(customerCommandsTopicName, settleBetCommand.getCustomerId(), settleFundsCommand);
        } else {
            BetSettledEvent betSettledEvent = new BetSettledEvent(settleBetCommand.getBetId(), settleBetCommand.getMarketId());
            kafkaTemplate.send(betSettleJobTopicName, settleBetCommand.getMarketId().toString(), betSettledEvent);
        }
    }

    @KafkaHandler
    @Transactional
    public void handleBetCreatedEvent(@Payload BetCreatedEvent event) {
        boolean isMarketClosed = betService.isMarketClosed(event.getMarketId());
        if (isMarketClosed) {
            RejectBetCommand rejectBetCommand = new RejectBetCommand(event.getBetId(), event.getCustomerId(),
                    "Bet %s was rejected, because market %s is already closed".formatted(event.getBetId(), event.getMarketId()));
            kafkaTemplate.send(betCommandsTopicName, event.getBetId().toString(), rejectBetCommand);
            return;
        }
        ReserveFundsCommand reserveFundsCommand = new ReserveFundsCommand(
                event.getBetId(),
                event.getCustomerId(),
                event.getMarketId(),
                event.getRequestId(),
                event.getCancelRequestId(),
                new BigDecimal(event.getStake())
        );
        kafkaTemplate.send(customerCommandsTopicName, event.getCustomerId(), reserveFundsCommand);
    }

    @KafkaHandler
    @Transactional
    public void handleUserBetPairMarketOpenedEvent(@Payload UserBetPairMarketOpenedEvent event) {
        BetData bet1 = createBet(event, event.getUser1Id(), MarketResult.ITEM1_WINS);
        BetData bet2 = createBet(event, event.getUser2Id(), MarketResult.ITEM2_WINS);
        betService.placeBet(bet1, bet1.getCustomerId());
        betService.placeBet(bet2, bet2.getCustomerId());
    }

    @KafkaHandler
    public void handleMarketOpenCheckCommand(@Payload MarketOpenCheckCommand command) {
        userItemStatusApplicationService.openMarket(command);
    }

    private BetData createBet(UserBetPairMarketOpenedEvent event, String userId, MarketResult marketResult) {
        return BetData.builder()
                .item1Id(event.getItem1Id())
                .item2Id(event.getItem2Id())
                .item1Name(event.getItem1Name())
                .item2Name(event.getItem2Name())
                .itemType(event.getItemType())
                .requestId(UUID.randomUUID())
                .cancelRequestId(UUID.randomUUID())
                .result(marketResult)
                .stake(1)
                .customerId(userId)
                .marketId(event.getMarketId())
                .build();
    }

}

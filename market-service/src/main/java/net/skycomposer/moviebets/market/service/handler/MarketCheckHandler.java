package net.skycomposer.moviebets.market.service.handler;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.skycomposer.moviebets.common.dto.market.MarketStatus;
import net.skycomposer.moviebets.common.dto.market.commands.CloseMarketCommand;
import net.skycomposer.moviebets.common.dto.market.commands.MarketCheckCommand;
import net.skycomposer.moviebets.common.dto.market.commands.MarketOpenCheckCommand;
import net.skycomposer.moviebets.market.dao.entity.MarketCheckEntity;
import net.skycomposer.moviebets.market.dao.entity.MarketEntity;
import net.skycomposer.moviebets.market.dao.repository.MarketCheckRepository;
import net.skycomposer.moviebets.market.dao.repository.MarketRepository;

@Component
@KafkaListener(topics = "${market.check.topic.name}", groupId = "${spring.kafka.consumer.market-check.group-id}")
public class MarketCheckHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String betSettleTopicName;

    private final String betCommandsTopicName;

    private final MarketRepository marketRepository;

    private final Long checkTimeThresholdSeconds;

    private final MarketCheckRepository marketCheckRepository;

    public MarketCheckHandler(
            MarketRepository marketRepository,
            MarketCheckRepository marketCheckRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${bet.settle.topic.name}") String betSettleTopicName,
            @Value("${bet.commands.topic.name}") String betCommandsTopicName,
            @Value("${market.check.check-time.threshold-seconds}") Long checkTimeThresholdSeconds
    ) {
        this.marketRepository = marketRepository;
        this.marketCheckRepository = marketCheckRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.betSettleTopicName = betSettleTopicName;
        this.betCommandsTopicName = betCommandsTopicName;
        this.checkTimeThresholdSeconds = checkTimeThresholdSeconds;
    }

    @KafkaHandler
    @Transactional
    public void handleMarketCheckCommand(@Payload MarketCheckCommand command) {
        MarketCheckEntity marketCheckEntity;
        boolean firstMarketCheck = false;
        boolean marketCheckUpdated = false;
        if (!marketCheckRepository.existsByCheckId(MarketCheckEntity.MARKET_CHECK_ID)) {
            marketCheckEntity = new MarketCheckEntity();
            firstMarketCheck = true;
            marketCheckUpdated = true;
        } else {
            marketCheckEntity = marketCheckRepository.findByCheckId(MarketCheckEntity.MARKET_CHECK_ID).get();
        }

        var now = Instant.now();
        var lastCheckTime = marketCheckEntity.getLastCheckAt();
        if (firstMarketCheck || lastCheckTime.plus(Duration.ofSeconds(checkTimeThresholdSeconds)).isBefore(now)) {
            marketCheckEntity.setLastCheckAt(now);
            marketCheckUpdated = true;
            marketCloseCheck(now);
            marketOpenCheck();
        }

        if (marketCheckUpdated) {
            marketCheckRepository.save(marketCheckEntity);
        }

    }

    private void marketCloseCheck(Instant now) {
        List<MarketEntity> openMarkets = marketRepository.findByStatus(MarketStatus.OPENED);

        for (MarketEntity market : openMarkets) {
            if (now.isAfter(market.getClosesAt())) {
                CloseMarketCommand closeMarketCommand = new CloseMarketCommand(market.getId());
                kafkaTemplate.send(betSettleTopicName, market.getId().toString(), closeMarketCommand);
            }
        }
    }

    private void marketOpenCheck() {
        MarketOpenCheckCommand command = new MarketOpenCheckCommand(MarketCheckEntity.MARKET_CHECK_ID);
        kafkaTemplate.send(betCommandsTopicName, MarketCheckEntity.MARKET_CHECK_ID.toString(), command);
    }
}

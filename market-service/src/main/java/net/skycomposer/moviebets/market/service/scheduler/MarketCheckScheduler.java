package net.skycomposer.moviebets.market.service.scheduler;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.skycomposer.moviebets.common.dto.market.commands.MarketCheckCommand;
import net.skycomposer.moviebets.market.dao.entity.MarketCheckEntity;
import net.skycomposer.moviebets.market.dao.repository.MarketCheckRepository;

@Component
public class MarketCheckScheduler {

    private final MarketCheckRepository marketCheckRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String marketCheckTopicName;

    private final Long checkTimeThresholdSeconds;

    public MarketCheckScheduler(
            MarketCheckRepository marketCheckRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${market.check.topic.name}") String marketCheckTopicName,
            @Value("${market.check.check-time.threshold-seconds}") Long checkTimeThresholdSeconds
    ) {
        this.marketCheckRepository = marketCheckRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.marketCheckTopicName = marketCheckTopicName;
        this.checkTimeThresholdSeconds = checkTimeThresholdSeconds;
    }

    @Scheduled(fixedRate = 10000) // Every 10 seconds
    @Transactional(readOnly = true)
    public void checkMarkets() {
        MarketCheckEntity marketCheckEntity;
        if (!marketCheckRepository.existsByCheckId(MarketCheckEntity.MARKET_CHECK_ID)) {
            marketCheckEntity = null;
        } else {
            marketCheckEntity = marketCheckRepository.findByCheckId(MarketCheckEntity.MARKET_CHECK_ID).get();
        }

        var now = Instant.now();
        var lastCheckTime = marketCheckEntity == null ? null : marketCheckEntity.getLastCheckAt();
        if (lastCheckTime == null || lastCheckTime.plus(Duration.ofSeconds(checkTimeThresholdSeconds)).isBefore(now)) {
            MarketCheckCommand marketCheckCommand = new MarketCheckCommand(MarketCheckEntity.MARKET_CHECK_ID);
            kafkaTemplate.send(marketCheckTopicName, MarketCheckEntity.MARKET_CHECK_ID.toString(), marketCheckCommand);
        }

    }



}

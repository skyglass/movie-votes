package net.skycomposer.moviebets.bet.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.bet.service.UserItemStatusService;
import net.skycomposer.moviebets.common.dto.bet.commands.UserItemStatusRequest;

@Component
@KafkaListener(topics = "${user.item-status.topic.name}", groupId = "${spring.kafka.consumer.item-status.group-id}")
@RequiredArgsConstructor
public class UserItemStatusHandler {

    private final UserItemStatusService userItemStatusService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @KafkaHandler
    public void handleCommand(@Payload UserItemStatusRequest userItemStatusRequest) {
        userItemStatusService.placeOrUpdateVote(userItemStatusRequest);
    }


}

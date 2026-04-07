package net.skycomposer.moviebets.bet.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.bet.service.UserItemVotesService;
import net.skycomposer.moviebets.common.dto.bet.commands.UserVotesUpdateCommand;

@Component
@KafkaListener(topics = "${user.votes.topic.name}", groupId = "${spring.kafka.consumer.user-votes.group-id}")
@RequiredArgsConstructor
public class UserVotesHandler {

    private final UserItemVotesService userItemVotesService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @KafkaHandler
    public void handleUserVotesUpdateCommand(@Payload UserVotesUpdateCommand userVotesUpdateCommand) {
        userItemVotesService.updateUserVotesAndFriendWeights(userVotesUpdateCommand);
    }


}


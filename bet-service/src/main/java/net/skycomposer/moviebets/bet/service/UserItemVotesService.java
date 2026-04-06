package net.skycomposer.moviebets.bet.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.bet.dao.repository.UserItemVotesRepository;
import net.skycomposer.moviebets.common.dto.bet.commands.UserVotesUpdateCommand;
import net.skycomposer.moviebets.common.dto.item.ItemType;
import net.skycomposer.moviebets.common.dto.votes.UserItemVotes;
import net.skycomposer.moviebets.common.dto.votes.UserItemVotesList;

@Service
@RequiredArgsConstructor
public class UserItemVotesService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserItemVotesRepository userItemVotesRepository;

    @Transactional
    public void updateUserVotesAndFriendWeights(UserVotesUpdateCommand command) {
        userItemVotesRepository.upsertUserVotes(
                command.getMarketId(), command.getItemWon(), command.getItemType().getValue(), true);
        userItemVotesRepository.upsertUserVotes(
                command.getMarketId(), command.getItemLost(), command.getItemType().getValue(), false);
        userItemVotesRepository.updateUserFriendWeights(command.getMarketId());
    }

    @Transactional(readOnly = true)
    public UserItemVotesList getRecommendedMoviesExcludingUserVoted(String userId) {
        List<UserItemVotes> result = userItemVotesRepository.findFriendWeightedVotesExcludingUserVoted(userId, ItemType.MOVIE);
        return new UserItemVotesList(result);
    }

    @Transactional(readOnly = true)
    public UserItemVotesList getRecommendedMovies(String userId) {
        List<UserItemVotes> result = userItemVotesRepository.findFriendWeightedVotesWithUserCanVoteFlag(userId, ItemType.MOVIE);
        return new UserItemVotesList(result);
    }

    @Transactional(readOnly = true)
    public UserItemVotesList getTopVotedMoviesExcludingUserVoted(String userId) {
        List<UserItemVotes> result = userItemVotesRepository.findTopVotedExcludingUserVoted(userId, ItemType.MOVIE);
        return new UserItemVotesList(result);
    }

    @Transactional(readOnly = true)
    public UserItemVotesList getTopVotedMovies(String userId) {
        List<UserItemVotes> result = userItemVotesRepository.findTopVotedWithUserCanVoteFlag(userId, ItemType.MOVIE);
        return new UserItemVotesList(result);
    }
}

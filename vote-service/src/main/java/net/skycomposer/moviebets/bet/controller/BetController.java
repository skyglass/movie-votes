package net.skycomposer.moviebets.bet.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.skycomposer.moviebets.bet.service.BetService;
import net.skycomposer.moviebets.bet.service.UserItemStatusService;
import net.skycomposer.moviebets.bet.service.UserItemVotesService;
import net.skycomposer.moviebets.bet.service.application.UserItemStatusApplicationService;
import net.skycomposer.moviebets.common.dto.bet.*;
import net.skycomposer.moviebets.common.dto.bet.commands.UserItemStatusRequest;
import net.skycomposer.moviebets.common.dto.item.UserItemVoteStatusList;
import net.skycomposer.moviebets.common.dto.movie.MovieIdsRequest;
import net.skycomposer.moviebets.common.dto.votes.UserItemVotesList;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BetController {

    private final BetService betService;

    private final UserItemVotesService userItemVotesService;

    private final UserItemStatusService userItemStatusService;

    private final UserItemStatusApplicationService userItemStatusApplicationService;

    @GetMapping("/get-market-status/{marketId}")
    public MarketStatusData getMarketStatus(@PathVariable UUID marketId, Authentication authentication) {
        String customerId = authentication.getName();
        return betService.getMarketStatus(marketId, customerId);
    }

    @GetMapping("/get-state/{betId}")
    public BetData getState(@PathVariable UUID betId) {
      return betService.getState(betId);
    }

    @PreAuthorize("hasRole('MOVIEBETS_MANAGER')")
    @GetMapping("/get-bets-by-market/{marketId}")
    public SumStakesData getBetsByMarket(@PathVariable UUID marketId) {
      return betService.getBetsByMarket(marketId);
    }

    @GetMapping("/get-bets-for-market/{marketId}")
    public BetDataList getBetsForMarket(@PathVariable UUID marketId) {
      return betService.getBetsForMarket(marketId, false);
    }

    @GetMapping("/get-recommended-movies")
    public UserItemVotesList getRecommendedMovies(Authentication authentication) {
        return userItemVotesService.getRecommendedMovies(authentication.getName());
    }

    @PostMapping("/get-movie-vote-statuses")
    public UserItemVoteStatusList getMovieVoteStatuses(@RequestBody MovieIdsRequest request, Authentication authentication) {
        return userItemStatusService.getMovieVoteStatuses(authentication.getName(), request.getIds());
    }

    @GetMapping("/get-recommended-movies-excluding-user-voted")
    public UserItemVotesList getRecommendedMoviesExcludingUserVoted(Authentication authentication) {
        return userItemVotesService.getRecommendedMoviesExcludingUserVoted(authentication.getName());
    }

    @GetMapping("/get-top-voted-movies")
    public UserItemVotesList getTopVotedMovies(Authentication authentication) {
        return userItemVotesService.getTopVotedMovies(authentication.getName());
    }

    @GetMapping("/get-top-voted-movies-excluding-user-voted")
    public UserItemVotesList getTopVotedMoviesExcludingUserVoted(Authentication authentication) {
        return userItemVotesService.getTopVotedMoviesExcludingUserVoted(authentication.getName());
    }

    @GetMapping("/get-open-markets")
    public PlaceBetDataList getOpenMarkets(Authentication authentication) {
        return betService.getOpenMarkets(authentication.getName());
    }

    @GetMapping("/get-bet-status/{customerId}/{marketId}")
    public BetStatusResponse getBetStatus(@PathVariable String customerId, @PathVariable UUID marketId) {
        return betService.getBetStatus(customerId, marketId);
    }

    @PreAuthorize("hasRole('MOVIEBETS_MANAGER')")
    @GetMapping("/get-bets-for-market-for-admin/{marketId}")
    public BetDataList getBetsForMarketForAdmin(@PathVariable UUID marketId) {
        return betService.getBetsForMarket(marketId, true);
    }

    @PreAuthorize("hasRole('MOVIEBETS_MANAGER') or #customerId == authentication.name")
    @GetMapping("/get-bets-for-player/{customerId}")
    public BetDataList getBetsForPlayer(@PathVariable String customerId) {
      return betService.getBetsForPlayer(customerId);
    }

    @PostMapping("/place")
    @ResponseStatus(HttpStatus.CREATED)
    public BetResponse place(@RequestBody @Valid BetData betData, Authentication authentication) {
        String authenticatedCustomerId = authentication.getName();
        return betService.placeBet(betData, authenticatedCustomerId);
    }

    @PostMapping("/place-request")
    @ResponseStatus(HttpStatus.CREATED)
    public UserItemStatusResponse placeRequest(@RequestBody @Valid UserItemStatusRequest userItemStatusRequest, Authentication authentication) {
        return userItemStatusApplicationService.placeVoteAsync(userItemStatusRequest, authentication.getName());
    }

    @PreAuthorize("hasRole('MOVIEBETS_MANAGER')")
    @PostMapping("/place-for-admin")
    @ResponseStatus(HttpStatus.CREATED)
    public BetResponse placeForAdmin(@RequestBody @Valid BetData betData) {
        return betService.placeBet(betData, betData.getCustomerId());
    }

    @PreAuthorize("hasRole('MOVIEBETS_MANAGER')")
    @PostMapping("/cancel-for-admin")
    public BetResponse cancelForAdmin(@RequestBody @Valid CancelBetRequest request, Authentication authentication) {
        String authenticatedCustomerId = authentication.getName();
        return betService.cancel(request, authenticatedCustomerId, true);
    }
}

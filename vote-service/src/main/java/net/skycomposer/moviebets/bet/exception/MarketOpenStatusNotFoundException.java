package net.skycomposer.moviebets.bet.exception;

import java.util.UUID;

public class MarketOpenStatusNotFoundException extends IllegalArgumentException {

    public MarketOpenStatusNotFoundException(UUID marketId) {
        super(String.format("Couldn't find market open status for market %s", marketId));
    }

}

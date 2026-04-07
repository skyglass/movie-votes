package net.skycomposer.moviebets.bet.exception;

public class MarketIsClosedException extends IllegalArgumentException {

    public MarketIsClosedException(String marketName) {
        super(String.format("Place Bet failed: Event '%s' is already closed", marketName));
    }

}

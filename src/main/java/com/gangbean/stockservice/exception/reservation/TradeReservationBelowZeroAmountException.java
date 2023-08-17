package com.gangbean.stockservice.exception.reservation;

public class TradeReservationBelowZeroAmountException extends TradeReservationException {
    public TradeReservationBelowZeroAmountException(String message) {
        super(message);
    }
}

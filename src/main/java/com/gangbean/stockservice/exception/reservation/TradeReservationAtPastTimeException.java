package com.gangbean.stockservice.exception.reservation;

public class TradeReservationAtPastTimeException extends TradeReservationException{
    public TradeReservationAtPastTimeException(String message) {
        super(message);
    }
}

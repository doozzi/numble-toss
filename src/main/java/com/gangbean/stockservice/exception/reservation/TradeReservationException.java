package com.gangbean.stockservice.exception.reservation;

import com.gangbean.stockservice.exception.StockServiceApplicationException;

public class TradeReservationException extends StockServiceApplicationException {
    public TradeReservationException(String message) {
        super(message);
    }
}

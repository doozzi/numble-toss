package com.gangbean.stockservice.exception.stock;

public class StockSellForBelowCurrentPriceException extends StockException {
    public StockSellForBelowCurrentPriceException(String message) {
        super(message);
    }
}

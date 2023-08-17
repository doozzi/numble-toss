package com.gangbean.stockservice.exception.stock;

public class StockBuyForOverCurrentPriceException extends StockException {
    public StockBuyForOverCurrentPriceException(String message) {
        super(message);
    }
}

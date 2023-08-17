package com.gangbean.stockservice.exception.stock;

public class StockNotEnoughBalanceException extends StockException {
    public StockNotEnoughBalanceException(String message) {
        super(message);
    }
}

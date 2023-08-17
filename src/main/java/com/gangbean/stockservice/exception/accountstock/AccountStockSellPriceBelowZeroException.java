package com.gangbean.stockservice.exception.accountstock;

public class AccountStockSellPriceBelowZeroException extends AccountStockException {
    public AccountStockSellPriceBelowZeroException(String message) {
        super(message);
    }
}

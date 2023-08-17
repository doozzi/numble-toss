package com.gangbean.stockservice.exception.accountstock;

public class AccountStockBuyPriceBelowZeroException extends AccountStockException {
    public AccountStockBuyPriceBelowZeroException(String message) {
        super(message);
    }
}

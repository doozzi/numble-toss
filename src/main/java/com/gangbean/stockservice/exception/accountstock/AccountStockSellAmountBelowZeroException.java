package com.gangbean.stockservice.exception.accountstock;

public class AccountStockSellAmountBelowZeroException extends AccountStockException {
    public AccountStockSellAmountBelowZeroException(String message) {
        super(message);
    }
}

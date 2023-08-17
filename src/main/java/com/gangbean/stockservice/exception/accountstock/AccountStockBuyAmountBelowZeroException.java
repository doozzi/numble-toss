package com.gangbean.stockservice.exception.accountstock;

public class AccountStockBuyAmountBelowZeroException extends AccountStockException {
    public AccountStockBuyAmountBelowZeroException(String message) {
        super(message);
    }
}

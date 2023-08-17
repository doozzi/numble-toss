package com.gangbean.stockservice.exception.accountstock;

public class AccountStockNotEnoughBalanceException extends AccountStockException {
    public AccountStockNotEnoughBalanceException(String message) {
        super(message);
    }
}

package com.gangbean.stockservice.exception.account;

public class TradeBetweenSameAccountsException extends AccountException {
    public TradeBetweenSameAccountsException(String message) {
        super(message);
    }
}

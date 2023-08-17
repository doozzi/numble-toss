package com.gangbean.stockservice.exception.account;

public class AccountNotEnoughBalanceException extends AccountException {
    public AccountNotEnoughBalanceException(String message) {
        super(message);
    }
}

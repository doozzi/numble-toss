package com.gangbean.stockservice.exception.account;

public class AccountNotExistsException extends AccountException {
    public AccountNotExistsException(String message) {
        super(message);
    }
}

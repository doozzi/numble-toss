package com.gangbean.stockservice.exception.account;

public class BankNotFoundException extends AccountException {

    public BankNotFoundException(String message) {
        super(message);
    }
}

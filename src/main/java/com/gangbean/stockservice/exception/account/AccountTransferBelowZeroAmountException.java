package com.gangbean.stockservice.exception.account;

public class AccountTransferBelowZeroAmountException extends AccountException {
    public AccountTransferBelowZeroAmountException(String message) {
        super(message);
    }
}

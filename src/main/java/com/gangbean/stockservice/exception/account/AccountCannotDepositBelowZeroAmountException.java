package com.gangbean.stockservice.exception.account;

public class AccountCannotDepositBelowZeroAmountException extends AccountException {

    public AccountCannotDepositBelowZeroAmountException(String message) {
        super(message);
    }
}

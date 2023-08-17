package com.gangbean.stockservice.exception.account;

public class AccountNotOwnedByLoginUser extends AccountException {
    public AccountNotOwnedByLoginUser(String message) {
        super(message);
    }
}

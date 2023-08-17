package com.gangbean.stockservice.exception.account;

import com.gangbean.stockservice.exception.StockServiceApplicationException;

public class AccountException extends StockServiceApplicationException {
    public AccountException(String message) {
        super(message);
    }
}

package com.gangbean.stockservice.exception.accountstock;

import com.gangbean.stockservice.exception.StockServiceApplicationException;

public class AccountStockException extends StockServiceApplicationException {
    public AccountStockException(String message) {
        super(message);
    }
}

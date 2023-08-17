package com.gangbean.stockservice.exception;

public class StockServiceApplicationException extends RuntimeException {
    public StockServiceApplicationException(String message) {
        super(message);
    }
}

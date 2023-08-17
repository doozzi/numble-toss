package com.gangbean.stockservice.exception.stock;

import com.gangbean.stockservice.exception.StockServiceApplicationException;

public class StockException extends StockServiceApplicationException {
    public StockException(String message) {
        super(message);
    }
}

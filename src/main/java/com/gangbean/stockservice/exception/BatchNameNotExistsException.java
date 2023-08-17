package com.gangbean.stockservice.exception;

public class BatchNameNotExistsException extends StockBatchException {
    public BatchNameNotExistsException(String message) {
        super(message);
    }
}

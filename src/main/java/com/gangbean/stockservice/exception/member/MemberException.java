package com.gangbean.stockservice.exception.member;

import com.gangbean.stockservice.exception.StockServiceApplicationException;

public class MemberException extends StockServiceApplicationException {
    public MemberException(String message) {
        super(message);
    }
}

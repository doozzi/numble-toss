package com.gangbean.stockservice.domain;

public enum StockTradeType {
    BUYING("구매"),
    SELLING("판매");

    private final String description;

    StockTradeType(String description) {
        this.description = description;
    }
}

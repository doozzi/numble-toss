package com.gangbean.stockservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gangbean.stockservice.domain.Stock;

import java.math.BigDecimal;
import java.util.Objects;

public class StockInfoResponse {

    private final Long id;

    private final String stockName;

    @JsonFormat(pattern = "#,###")
    private final BigDecimal price;

    @JsonFormat(pattern = "#,###")
    private final BigDecimal balance;

    public StockInfoResponse(Long id, String stockName, BigDecimal price, BigDecimal balance) {
        this.id = id;
        this.stockName = stockName;
        this.price = price;
        this.balance = balance;
    }

    public static StockInfoResponse responseOf(Stock stock) {
        return new StockInfoResponse(stock.id(), stock.name(), stock.howMuch(), stock.howMany());
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getStockName() {
        return stockName;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockInfoResponse that = (StockInfoResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(stockName, that.stockName) && Objects.equals(price, that.price) && Objects.equals(balance, that.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, stockName, price, balance);
    }
}

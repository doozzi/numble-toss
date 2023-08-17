package com.gangbean.stockservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class StockSellRequest {

    private BigDecimal amount;

    private BigDecimal price;

    public StockSellRequest(BigDecimal amount, BigDecimal price) {
        this.amount = amount;
        this.price = price;
    }
}

package com.gangbean.stockservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gangbean.stockservice.domain.AccountStock;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class StockBuyResponse {

    private Long stockId;

    @JsonFormat(pattern = "#,###")
    private BigDecimal amount;

    @JsonFormat(pattern = "#,###")
    private BigDecimal averagePrice;

    public StockBuyResponse(Long stockId, BigDecimal amount, BigDecimal averagePrice) {
        this.stockId = stockId;
        this.amount = amount;
        this.averagePrice = averagePrice;
    }

    public static StockBuyResponse responseOf(AccountStock accountStock) {
        return new StockBuyResponse(accountStock.what().id(), accountStock.howMany(), accountStock.howMuch());
    }
}

package com.gangbean.stockservice.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StockBuyRequest {

    private BigDecimal amount;

    private BigDecimal price;
}

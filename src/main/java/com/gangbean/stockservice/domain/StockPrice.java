package com.gangbean.stockservice.domain;

import java.math.BigDecimal;

@FunctionalInterface
public interface StockPrice {
    BigDecimal generateNextPrice(BigDecimal prev);
}

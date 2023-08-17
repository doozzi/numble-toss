package com.gangbean.stockservice.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class StockRandomPrice implements StockPrice {

    private static final int RANGE = 3;

    private static final Random RANDOM = new Random();

    @Override
    public BigDecimal generateNextPrice(BigDecimal prev) {
        double rate = RANDOM.nextDouble() * RANGE * (RANDOM.nextBoolean() ? 1 : -1);
        return prev.multiply(BigDecimal.valueOf(100 + rate))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }
}

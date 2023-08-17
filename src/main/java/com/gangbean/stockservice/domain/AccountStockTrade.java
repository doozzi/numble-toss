package com.gangbean.stockservice.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class AccountStockTrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private StockTradeType tradeType;

    private BigDecimal amount;

    private BigDecimal price;

    private LocalDateTime tradeAt;

    public AccountStockTrade() {
    }

    public AccountStockTrade(StockTradeType tradeType, BigDecimal amount, BigDecimal price, LocalDateTime tradeAt) {
        this.tradeType = tradeType;
        this.amount = amount;
        this.price = price;
        this.tradeAt = tradeAt;
    }

    public AccountStockTrade(Long id, StockTradeType tradeType, BigDecimal amount, BigDecimal price, LocalDateTime tradeAt) {
        this(tradeType, amount, price, tradeAt);
        this.id = id;
    }

    public Long id() {
        return id;
    }

    public BigDecimal amount() {
        return amount;
    }

    public BigDecimal price() {
        return price;
    }

    public StockTradeType tradeType() {
        return tradeType;
    }

    public LocalDateTime when() {
        return tradeAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountStockTrade that = (AccountStockTrade) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AccountStockTrade{" +
                "id=" + id +
                ", tradeType=" + tradeType +
                ", amount=" + amount +
                ", price=" + price +
                ", tradeAt=" + tradeAt +
                '}';
    }
}

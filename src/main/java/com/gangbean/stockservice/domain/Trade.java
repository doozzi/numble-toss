package com.gangbean.stockservice.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateTime;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TradeType type;

    public Trade() {
    }

    public Trade(TradeType type, LocalDateTime dateTime, BigDecimal amount) {
        this.type = type;
        this.dateTime = dateTime;
        this.amount = amount;
    }

    public Trade(Long id, TradeType type, LocalDateTime dateTime, BigDecimal amount) {
        this.id = id;
        this.type = type;
        this.dateTime = dateTime;
        this.amount = amount;
    }

    public LocalDateTime when() {
        return dateTime;
    }

    public BigDecimal howMuch() {
        return amount;
    }

    public TradeType how() {
        return type;
    }

    public Long id() {
        return id;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "id=" + id +
                ", dateTime=" + dateTime +
                ", amount=" + amount +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        return Objects.equals(id, trade.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

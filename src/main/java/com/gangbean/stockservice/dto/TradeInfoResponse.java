package com.gangbean.stockservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gangbean.stockservice.domain.Trade;
import com.gangbean.stockservice.domain.TradeType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@NoArgsConstructor
public class TradeInfoResponse {

    private Long id;

    private TradeType tradeType;

    private LocalDateTime tradeAt;

    @JsonFormat(pattern = "#,###")
    private BigDecimal amount;

    public TradeInfoResponse(Long id, TradeType tradeType, LocalDateTime tradeAt, BigDecimal amount) {
        this.id = id;
        this.tradeType = tradeType;
        this.tradeAt = tradeAt;
        this.amount = amount;
    }

    public static TradeInfoResponse responseOf(Trade trade) {
        return new TradeInfoResponse(trade.id(), trade.how(), trade.when(), trade.howMuch());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeInfoResponse that = (TradeInfoResponse) o;
        return Objects.equals(id, that.id) && tradeType == that.tradeType && Objects.equals(tradeAt, that.tradeAt) && Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tradeType, tradeAt, amount);
    }
}

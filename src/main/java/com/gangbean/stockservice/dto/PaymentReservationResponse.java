package com.gangbean.stockservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gangbean.stockservice.domain.TradeReservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentReservationResponse {

    private final Long accountId;

    @JsonFormat(pattern = "#,###")
    private final BigDecimal balance;

    private final LocalDateTime sendAt;

    public PaymentReservationResponse(Long accountId, BigDecimal balance, LocalDateTime sendAt) {
        this.accountId = accountId;
        this.balance = balance;
        this.sendAt = sendAt;
    }

    public static PaymentReservationResponse responseOf(TradeReservation tradeReservation) {
        return new PaymentReservationResponse(tradeReservation.from().id()
                , tradeReservation.from().balance(), tradeReservation.when());
    }

    public Long getAccountId() {
        return accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public LocalDateTime getSendAt() {
        return sendAt;
    }
}

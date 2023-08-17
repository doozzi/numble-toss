package com.gangbean.stockservice.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class PaymentReservationRequest {

    private BigDecimal amount;

    private LocalDateTime sendAt;
}

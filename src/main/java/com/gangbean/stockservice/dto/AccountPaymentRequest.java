package com.gangbean.stockservice.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class AccountPaymentRequest {

    private BigDecimal price;
}

package com.gangbean.stockservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class AccountTransferRequest {

    private String receiverAccountNumber;

    private BigDecimal amount;
}

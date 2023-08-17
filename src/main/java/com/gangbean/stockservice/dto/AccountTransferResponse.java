package com.gangbean.stockservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class AccountTransferResponse {

    @JsonFormat(pattern = "#,###")
    private BigDecimal balance;

    public AccountTransferResponse(BigDecimal balance) {
        this.balance = balance;
    }

    public static AccountTransferResponse responseOf(BigDecimal balance) {
        return new AccountTransferResponse(balance);
    }

    public BigDecimal balance() {
        return balance;
    }
}

package com.gangbean.stockservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gangbean.stockservice.domain.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AccountPaymentResponse {

    private final Long accountId;

    @JsonFormat(pattern = "#,###")
    private final BigDecimal balance;

    public static AccountPaymentResponse responseOf(Account account) {
        return new AccountPaymentResponse(account.id(), account.balance());
    }

    public BigDecimal balance() {
        return balance;
    }
}

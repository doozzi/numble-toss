package com.gangbean.stockservice.dto;

import com.gangbean.stockservice.domain.Account;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class AccountOpenRequest {
    private String bankName;
    private Long bankNumber;
    private BigDecimal balance;

    public AccountOpenRequest(String bankName, Long bankNumber, BigDecimal balance, Long memberId) {
        this.bankName = bankName;
        this.bankNumber = bankNumber;
        this.balance = balance;
    }

    public static AccountOpenRequest requestOf(Account account) {
        return new AccountOpenRequest(account.bank().name(), account.bank().number(), account.balance(), account.whose().getId());
    }

    public String bankName() {
        return bankName;
    }

    public Long bankNumber() {
        return bankNumber;
    }

    public BigDecimal balance() {
        return balance;
    }
}

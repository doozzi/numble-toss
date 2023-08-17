package com.gangbean.stockservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gangbean.stockservice.domain.Account;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;


@Getter
@NoArgsConstructor
public class AccountInfoResponse {

    private Long id;

    private String accountNumber;

    private String bankName;

    private Long bankNumber;

    @JsonFormat(pattern = "#")
    private BigDecimal balance;

    public AccountInfoResponse(Long id, String accountNumber, String bankName, Long bankNumber, BigDecimal balance) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.bankNumber = bankNumber;
        this.balance = balance;
    }

    public Long id() {
        return id;
    }

    public String accountNumber() {
        return accountNumber;
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

    public static AccountInfoResponse responseOf(Account account) {
        return new AccountInfoResponse(account.id()
                , account.number()
                , account.bank().name()
                , account.bank().number()
                , account.balance());
    }

    @Override
    public String toString() {
        return "AccountInfoResponse{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", bankName='" + bankName + '\'' +
                ", bankNumber=" + bankNumber +
                ", balance=" + balance +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountInfoResponse that = (AccountInfoResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(accountNumber, that.accountNumber) && Objects.equals(bankName, that.bankName) && Objects.equals(bankNumber, that.bankNumber) && Objects.equals(balance, that.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountNumber, bankName, bankNumber, balance);
    }
}

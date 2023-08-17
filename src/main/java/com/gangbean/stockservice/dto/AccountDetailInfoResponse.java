package com.gangbean.stockservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gangbean.stockservice.domain.Account;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class AccountDetailInfoResponse {

    private Long id;
    private String accountNumber;
    private String bankName;
    private Long bankNumber;

    @JsonFormat(pattern = "#,###")
    private BigDecimal balance;
    private List<TradeInfoResponse> trades;

    public AccountDetailInfoResponse(Long id, String accountNumber, String bankName, Long bankNumber, BigDecimal balance, List<TradeInfoResponse> trades) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.bankNumber = bankNumber;
        this.balance = balance;
        this.trades = trades;
    }

    public static AccountDetailInfoResponse responseOf(Account account) {
        return new AccountDetailInfoResponse(account.id(),
                account.number(),
                account.bank().name(),
                account.bank().number(),
                account.balance(),
                account.trades().stream().map(TradeInfoResponse::responseOf).collect(Collectors.toList()));
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

    public List<TradeInfoResponse> trades() {
        return trades;
    }

    public Long lastIndex() {
        return (trades.size() == 0) ? 0 : trades.get(trades().size() - 1).getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountDetailInfoResponse that = (AccountDetailInfoResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(accountNumber, that.accountNumber) && Objects.equals(bankName, that.bankName) && Objects.equals(bankNumber, that.bankNumber) && Objects.equals(balance, that.balance) && Objects.equals(trades, that.trades);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountNumber, bankName, bankNumber, balance, trades);
    }
}

package com.gangbean.stockservice.dto;

import com.gangbean.stockservice.domain.Account;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class AccountInfoListResponse {

    private List<AccountInfoResponse> accounts;

    public AccountInfoListResponse(List<AccountInfoResponse> accounts) {
        this.accounts = accounts;
    }

    public static AccountInfoListResponse responseOf(List<Account> list) {
        return new AccountInfoListResponse(list.stream()
            .map(AccountInfoResponse::responseOf)
            .collect(Collectors.toList()));
    }

    public List<AccountInfoResponse> accountInfoList() {
        return accounts;
    }

    public Long lastIndex() {
        return (accounts.size() == 0) ? 0 : accounts.get(accounts.size() - 1).getId();
    }

    @Override
    public String toString() {
        return "AccountInfoListResponse{" +
                "list=" + accounts +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountInfoListResponse that = (AccountInfoListResponse) o;
        return Objects.equals(accounts, that.accounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accounts);
    }
}

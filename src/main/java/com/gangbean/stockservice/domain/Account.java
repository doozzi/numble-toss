package com.gangbean.stockservice.domain;

import com.gangbean.stockservice.exception.account.AccountCannotDepositBelowZeroAmountException;
import com.gangbean.stockservice.exception.account.AccountNotEnoughBalanceException;
import com.gangbean.stockservice.exception.account.AccountNotOwnedByLoginUser;
import com.gangbean.stockservice.exception.account.AccountTransferBelowZeroAmountException;
import com.gangbean.stockservice.exception.accountstock.AccountStockNotExistsException;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 14)
    private String number;

    @ManyToOne
    private Member member;

    @ManyToOne
    private Bank bank;

    private BigDecimal balance;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<Trade> trades;

    @OrderBy("id desc")
    @OneToMany(cascade = CascadeType.ALL)
    private Set<AccountStock> stocks;

    public Account() {}

    public Account(String number, Member member, Bank bank, BigDecimal balance, Set<Trade> trades, Set<AccountStock> stocks) {
        this.number = number;
        this.member = member;
        this.bank = bank;
        this.balance = moreThanZero(balance);
        this.trades = trades;
        this.stocks = stocks;
    }

    public Account(Long id, String number, Member member, Bank bank, BigDecimal balance, Set<Trade> trades, Set<AccountStock> stocks) {
        this(number, member, bank, balance, trades, stocks);
        this.id = id;
    }

    public Long id() {
        return id;
    }

    public String number() {
        return number;
    }

    public Bank bank() {
        return bank;
    }

    public BigDecimal balance() {
        return balance;
    }

    public Account ownedBy(Member member) {
        if (!isOwner(member)) {
            throw new AccountNotOwnedByLoginUser("본인의 계좌가 아닙니다: " + id);
        }
        return this;
    }

    public Member whose() {
        return member;
    }

    public Set<Trade> trades() {
        return trades;
    }

    public void deposit(LocalDateTime tradeAt, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountCannotDepositBelowZeroAmountException("계좌는 0원 이하 금액을 입금할 수 없습니다: " + amount);
        }
        balance = balance.add(amount);
        trades.add(new Trade(TradeType.DEPOSIT, tradeAt, amount));
    }

    public void withDraw(LocalDateTime tradeAt, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountTransferBelowZeroAmountException("계좌는 0원 이하의 금액은 송금할 수 없습니다: " + amount);
        }
        if (amount.compareTo(balance) > 0) {
            throw new AccountNotEnoughBalanceException("계좌 잔액이 부족합니다: " + balance);
        }
        balance = balance.subtract(amount);
        trades.add(new Trade(TradeType.WITHDRAW, tradeAt, amount));
    }

    public void pay(LocalDateTime tradeAt, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountTransferBelowZeroAmountException("계좌는 0원 이하의 금액은 결제할 수 없습니다: " + amount);
        }
        if (amount.compareTo(balance) > 0) {
            throw new AccountNotEnoughBalanceException("계좌 잔액이 부족합니다: " + balance);
        }
        balance = balance.subtract(amount);
        trades.add(new Trade(TradeType.PAYMENT, tradeAt, amount));
    }

    public void buyStock(Stock marketStock, BigDecimal price, BigDecimal amount, LocalDateTime buyAt) {
        AccountStock accountStock = myStock(marketStock)
            .orElseGet(() -> emptyAccountStock(marketStock));
        accountStock.buy(price, amount, buyAt);
        this.pay(buyAt, price.multiply(amount));
    }

    public void sellStock(Stock marketStock, BigDecimal price, BigDecimal amount, LocalDateTime sellAt) {
        AccountStock accountStock = myStock(marketStock)
            .orElseThrow(() -> new AccountStockNotExistsException("보유한 주식이 아닙니다: " + marketStock.id()));
        accountStock.sell(price, amount, sellAt);
        this.deposit(sellAt, price.multiply(amount));
    }

    public void payReservation(LocalDateTime tradeAt, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountTransferBelowZeroAmountException("계좌는 0원 이하의 금액은 결제할 수 없습니다: " + amount);
        }
        if (amount.compareTo(balance) > 0) {
            throw new AccountNotEnoughBalanceException("계좌 잔액이 부족합니다: " + balance);
        }
        balance = balance.subtract(amount);
        trades.add(new Trade(TradeType.RESERVATION, tradeAt, amount));
    }

    public Optional<AccountStock> myStock(Long stockId) {
        return stocks.stream()
            .filter(accountStock -> Objects.equals(accountStock.what().id(), stockId))
            .findAny();
    }

    public boolean isOverBalance(BigDecimal amount) {
        return balance.compareTo(amount) < 0;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", member=" + member +
                ", bank=" + bank +
                ", balance=" + balance +
                ", trades=" + trades +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private boolean isOwner(Member member) {
        return this.member.equals(member);
    }

    private BigDecimal moreThanZero(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountNotEnoughBalanceException("0원 미만의 금액은 입금할 수 없습니다: " + balance);
        }
        return balance;
    }

    private Optional<AccountStock> myStock(Stock marketStock) {
        return stocks.stream()
            .filter(stock -> Objects.equals(stock.what(), marketStock))
            .findAny();
    }

    private AccountStock emptyAccountStock(Stock marketStock) {
        AccountStock newAccountStock
            = new AccountStock(marketStock, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new HashSet<>());
        stocks.add(newAccountStock);
        return newAccountStock;
    }
}

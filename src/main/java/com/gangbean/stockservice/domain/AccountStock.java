package com.gangbean.stockservice.domain;

import com.gangbean.stockservice.exception.accountstock.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Entity
public class AccountStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Stock stock;

    private BigDecimal balance;

    private BigDecimal price;

    private BigDecimal totalPaid;

    @OrderBy("tradeAt desc")
    @OneToMany(cascade = CascadeType.ALL)
    private Set<AccountStockTrade> history;

    public AccountStock() {}

    public AccountStock(Stock stock, BigDecimal balance, BigDecimal price, BigDecimal totalPaid, Set<AccountStockTrade> history) {
        this.stock = stock;
        this.balance = balance;
        this.price = price;
        this.totalPaid = totalPaid;
        this.history = history;
    }

    public AccountStock(Long id, Stock stock, BigDecimal balance, BigDecimal price, BigDecimal totalPaid, Set<AccountStockTrade> history) {
        this(stock, balance, price, totalPaid, history);
        this.id = id;
    }

    public Long id() {
        return id;
    }

    public Stock what() {
        return stock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountStock that = (AccountStock) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public BigDecimal howMany() {
        return balance;
    }

    public BigDecimal howMuch() {
        return price;
    }

    public BigDecimal howMuchPaid() {
        return totalPaid;
    }

    public Set<AccountStockTrade> history() {
        return history;
    }

    public void sell(BigDecimal price, BigDecimal amount, LocalDateTime tradeAt) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountStockSellAmountBelowZeroException("0이하의 개수는 판매할 수 없습니다: " + amount);
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountStockSellPriceBelowZeroException("0이하의 금액으로 판매할 수 없습니다: " + price);
        }
        if (balance.compareTo(amount) < 0) {
            throw new AccountStockNotEnoughBalanceException("보유수량이 부족합니다: " + balance);
        }
        balance = balance.subtract(amount);
        totalPaid = totalPaid.subtract(price.multiply(amount));
        if (balance.compareTo(BigDecimal.ZERO) == 0) {
            totalPaid = BigDecimal.ZERO;
            this.price = BigDecimal.ZERO;
        }
        history.add(new AccountStockTrade(StockTradeType.SELLING, amount, price, tradeAt));
    }

    public void buy(BigDecimal price, BigDecimal amount, LocalDateTime tradeAt) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountStockBuyAmountBelowZeroException("0이하의 개수는 구매할 수 없습니다: " + amount);
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountStockBuyPriceBelowZeroException("0이하의 금액으로 구매할 수 없습니다: " + price);
        }
        balance = balance.add(amount);
        totalPaid = totalPaid.add(price.multiply(amount));
        this.price = totalPaid.divide(balance, 0, RoundingMode.DOWN);
        history.add(new AccountStockTrade(StockTradeType.BUYING, amount, price, tradeAt));
    }
}

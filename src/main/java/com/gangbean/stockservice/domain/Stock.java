package com.gangbean.stockservice.domain;

import com.gangbean.stockservice.exception.stock.StockAmountNotValidException;
import com.gangbean.stockservice.exception.stock.StockBuyForOverCurrentPriceException;
import com.gangbean.stockservice.exception.stock.StockNotEnoughBalanceException;
import com.gangbean.stockservice.exception.stock.StockSellForBelowCurrentPriceException;
import com.gangbean.stockservice.util.StringUtil;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Entity
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private BigDecimal price;

    private BigDecimal balance;

    @OrderBy("writtenAt desc")
    @OneToMany(cascade = CascadeType.ALL)
    private Set<StockHistory> histories;

    public Stock() {}

    public Stock(Long id, String name, BigDecimal price, BigDecimal balance, Set<StockHistory> histories) {
        this(name, price, balance, histories);
        this.id = id;
    }

    public Stock(String name, BigDecimal price, BigDecimal balance, Set<StockHistory> histories) {
        this.name = name;
        this.price = price;
        this.balance = balance;
        this.histories = histories;
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public BigDecimal howMuch() {
        return price;
    }

    public BigDecimal howMany() {
        return balance;
    }

    public Set<StockHistory> histories() {
        return histories;
    }

    public void sell(BigDecimal price, BigDecimal amount) {
        if (this.price.compareTo(price) > 0) {
            throw new StockSellForBelowCurrentPriceException("주식의 현재가격보다 낮은 가격으로 구매할 수 없습니다: " + this.price);
        }
        if (balance.compareTo(amount) < 0) {
            throw new StockNotEnoughBalanceException("주식의 잔량이 부족합니다: " + balance);
        }
        balance = balance.subtract(amount);
    }

    public void buy(BigDecimal price, BigDecimal amount) {
        if (this.price.compareTo(price) < 0) {
            throw new StockBuyForOverCurrentPriceException("주식의 현재가격보다 높은 가격으로 판매할 수 없습니다: " + this.price);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new StockAmountNotValidException("1개 이상만 구매가능합니다: " + amount);
        }
        balance = balance.add(amount);
    }

    public void updatePrice(StockPrice stockPrice, LocalDateTime now) {
        BigDecimal before = price;
        histories.add(new StockHistory(now, price));
        price = stockPrice.generateNextPrice(price);
        announceResult(before);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return Objects.equals(id, stock.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private void announceResult(BigDecimal before) {
        System.out.println(">>>>>>>>> | " + StringUtil.paddLeftWith(name,10) + " | "
                + StringUtil.paddLeftWith(new DecimalFormat("#,###").format(before.divide(BigDecimal.ONE, 0, RoundingMode.DOWN)),16)
                + " | " + StringUtil.paddLeftWith(new DecimalFormat("#,###").format(price), 16)
                + " | " + StringUtil.paddLeftWith(new DecimalFormat("+#,###;-#,###").format(before.subtract(price)),16)
                + " | (" + new DecimalFormat("+#.###;-#.###").format(before.subtract(price).divide(before, 3, RoundingMode.HALF_UP).multiply(new BigDecimal(100))) + " %)"
                + " | ");
    }
}

package com.gangbean.stockservice.domain;

import com.gangbean.stockservice.exception.StockServiceApplicationException;
import com.gangbean.stockservice.exception.account.AccountNotEnoughBalanceException;
import com.gangbean.stockservice.exception.reservation.TradeReservationAtPastTimeException;
import com.gangbean.stockservice.exception.reservation.TradeReservationBelowZeroAmountException;
import com.gangbean.stockservice.exception.reservation.TradeReservationNotHourlyBasisTimeException;
import com.gangbean.stockservice.util.BatchExecutionTime;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
public class TradeReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Account account;

    private LocalDateTime tradeAt;

    private BigDecimal amount;

    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;

    public TradeReservation() {}

    public TradeReservation(Account account, LocalDateTime tradeAt, BigDecimal amount) {
        this.account = enoughBalanced(account, amount);
        this.tradeAt = hourlyBasis(tradeAt);
        this.amount = aboveZero(amount);
        this.status = ReservationStatus.READY;
    }

    public TradeReservation(Long id, Account account, LocalDateTime tradeAt, BigDecimal amount) {
        this(account, tradeAt, amount);
        this.id = id;
    }

    public Account from() {
        return account;
    }

    public BigDecimal howMuch() {
        return amount;
    }

    public LocalDateTime when() {
        return tradeAt;
    }

    public Long id() {
        return id;
    }

    public void execute() {
        try {
            account.payReservation(tradeAt, amount);
            status = status.complete();
        } catch (StockServiceApplicationException e) {
            status = status.error();
        }

        System.out.println(">>>>>>> complete trade " + this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeReservation that = (TradeReservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TradeReservation{" +
                "id=" + id +
                ", tradeAt=" + tradeAt +
                ", amount=" + amount +
                '}';
    }

    private BigDecimal aboveZero(BigDecimal amount) {
        if (belowBasis(amount)) {
            throw new TradeReservationBelowZeroAmountException("0이하의 금액은 예약불가합니다: " + amount);
        }
        return amount;
    }

    private boolean belowBasis(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) <= 0;
    }

    private LocalDateTime hourlyBasis(LocalDateTime tradeAt) {
        if (isNotHourlyBasis(tradeAt)) {
            throw new TradeReservationNotHourlyBasisTimeException("결제예약은 매 시간단위로만 요청가능합니다: " + tradeAt);
        }
        if (BatchExecutionTime.isExecutionImpossibleAt("Reservation", tradeAt)) {
            throw new TradeReservationAtPastTimeException("다음 수행 예정시각 이전으로 예약할 수 없습니다: " + BatchExecutionTime.nextExecutionTime("Reservation"));
        }
        return tradeAt;
    }

    private static boolean isNotHourlyBasis(LocalDateTime tradeAt) {
        return !tradeAt.truncatedTo(ChronoUnit.HOURS).equals(tradeAt);
    }

    private Account enoughBalanced(Account account, BigDecimal amount) {
        if (account.isOverBalance(amount)) {
            throw new AccountNotEnoughBalanceException("계좌의 잔액이 부족합니다: " + account.balance());
        }
        return account;
    }
}

package com.gangbean.stockservice.domain


import com.gangbean.stockservice.exception.account.AccountNotEnoughBalanceException
import com.gangbean.stockservice.exception.reservation.TradeReservationBelowZeroAmountException
import com.gangbean.stockservice.exception.reservation.TradeReservationNotHourlyBasisTimeException
import com.gangbean.stockservice.util.BatchExecutionTime
import spock.lang.Specification

import java.time.LocalDateTime

import static com.gangbean.stockservice.domain.MemberTest.TEST_MEMBER

class TradeReservationTest extends Specification {

    def setup() {
        BatchExecutionTime.write("Reservation", LocalDateTime.MIN)
    }

    def "결제예약은 계좌의 잔액을 넘는 금액은 거절합니다"() {
        given:
        Long id = 1L
        LocalDateTime tradeAt = LocalDateTime.of(2023, 12, 30, 1, 0)
        Long balance = 1_000L
        Account account = new Account(1L, "1", TEST_MEMBER, new Bank("은행", 1L), balance as BigDecimal, new HashSet<>(), new HashSet<>())

        when:
        Long amount = 10_000L
        def reservation = new TradeReservation(id, account, tradeAt, amount as BigDecimal)

        then:
        def error = thrown(AccountNotEnoughBalanceException.class)

        expect:
        balance < amount
        error.getMessage() == "계좌의 잔액이 부족합니다: " + balance
    }

    def "결제예약은 계좌를 요구하고 반환합니다"() {
        given:
        Long id = 1L
        LocalDateTime tradeAt = LocalDateTime.of(2023, 12, 30, 1, 0)
        Account account = new Account(1L, "1", TEST_MEMBER, new Bank("은행", 1L), 1000L as BigDecimal, new HashSet<>(), new HashSet<>())

        when:
        def reservation = new TradeReservation(id, account, tradeAt, 100L as BigDecimal)

        then:
        reservation.from() == account
    }

    def "결제예약은 0이하의 금액은 거절합니다"(Long amount) {
        given:
        Long id = 1L
        LocalDateTime tradeAt = LocalDateTime.of(2023, 12, 30, 1, 0)
        Account account = new Account(1L, "1", TEST_MEMBER, new Bank("은행", 1L), 1000L as BigDecimal, new HashSet<>(), new HashSet<>())

        when:
        def reservation = new TradeReservation(id, account, tradeAt, amount as BigDecimal)

        then:
        def error = thrown(TradeReservationBelowZeroAmountException.class)

        expect:
        error.getMessage() == "0이하의 금액은 예약불가합니다: " + amount

        where:
        amount << [-100L, -1000L, -1L]
    }

    def "결제예약은 금액을 요구하고 반환합니다"() {
        given:
        Long id = 1L
        LocalDateTime tradeAt = LocalDateTime.of(2023, 12, 30, 1, 0)
        Long amount = 1_000L
        Account account = new Account(1L, "1", TEST_MEMBER, new Bank("은행", 1L), 1000L as BigDecimal, new HashSet<>(), new HashSet<>())

        when:
        def reservation = new TradeReservation(id, account, tradeAt, 1_000L as BigDecimal)

        then:
        reservation.howMuch() == amount
    }

    def "결제예약은 시간단위의 예약만 허용합니다"(int hour, int minute) {
        given:
        Long id = 1L
        LocalDateTime tradeAt = LocalDateTime.of(2023, 12, 30, hour, minute)
        Account account = new Account(1L, "1", TEST_MEMBER, new Bank("은행", 1L), 1000L as BigDecimal, new HashSet<>(), new HashSet<>())

        when:
        new TradeReservation(id, account, tradeAt, 100L as BigDecimal)

        then:
        def error = thrown(TradeReservationNotHourlyBasisTimeException.class)

        expect:
        error.getMessage() == "결제예약은 매 시간단위로만 요청가능합니다: " + tradeAt

        where:
        hour | minute
        1    |   1
        10   |   59
    }

    def "결제예약은 예약시간을 요구하고, 반환합니다"() {
        given:
        Long id = 1L
        LocalDateTime tradeAt = LocalDateTime.of(2023, 12, 30, 14, 00)
        Account account = new Account(1L, "1", TEST_MEMBER, new Bank("은행", 1L), 1000L as BigDecimal, new HashSet<>(), new HashSet<>())

        when:
        def reservation = new TradeReservation(id, account, tradeAt, 100L as BigDecimal)

        then:
        verifyAll {
            reservation.when() == tradeAt
        }
    }

    def "결제에약은 id를 반환합니다"() {
        given:
        Long id = 1L;
        Account account = new Account(1L, "1", TEST_MEMBER, new Bank("은행", 1L), 1000L as BigDecimal, new HashSet<>(), new HashSet<>())

        when:
        def reservation = new TradeReservation(id, account, LocalDateTime.of(2023, 7, 1, 14, 0), 100L as BigDecimal)

        then:
        reservation.id() == id
    }

    def "결제예약은 id를 요구합니다"() {
        when:
        new TradeReservation(1L, new Account(1L, "1", TEST_MEMBER, new Bank("은행", 1L), 1000L as BigDecimal, new HashSet<>(), new HashSet<>()), LocalDateTime.of(2023, 7, 1, 14, 0), 100L as BigDecimal)

        then:
        noExceptionThrown()
    }
}

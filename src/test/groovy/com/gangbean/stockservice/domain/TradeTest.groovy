package com.gangbean.stockservice.domain


import spock.lang.Specification

import java.time.LocalDateTime

import static com.gangbean.stockservice.domain.MemberTest.*

class TradeTest extends Specification {
    def "거래는 본인의 식별자를 알려줍니다"() {
        given:
        Long id = 1L
        Long amount = 1_000L
        LocalDateTime tradeAt = LocalDateTime.of(2023,07,01,14,0)
        TradeType type = TradeType.DEPOSIT
        Account account = new Account(1L, "00000", TEST_MEMBER, new Bank(1L, "은행", 1L), 1_000L, new HashSet<>(), new HashSet<>())

        when:
        Trade trade = new Trade(id, type, tradeAt, amount)

        then:
        trade.id() == id
    }

    def "거래는 어떻게 이루어졌는지 알려줍니다"() {
        given:
        Long amount = 1_000L
        LocalDateTime tradeAt = LocalDateTime.of(2023,07,01,14,0)
        TradeType type = TradeType.DEPOSIT
        Account account = new Account(1L, "00000", TEST_MEMBER, new Bank(1L, "은행", 1L), 1_000L, new HashSet<>(), new HashSet<>())

        when:
        Trade trade = new Trade(1L, type, tradeAt, amount)

        then:
        trade.how() == type
    }

    def "거래는 얼마나 주고받았는지 알려줍니다"() {
        given:
        Long amount = 1_000L
        LocalDateTime tradeAt = LocalDateTime.of(2023,07,01,14,0)
        Account account = new Account(1L, "00000", TEST_MEMBER, new Bank(1L, "은행", 1L), 1_000L, new HashSet<>(), new HashSet<>())

        when:
        Trade trade = new Trade(1L, TradeType.WITHDRAW, tradeAt, amount)

        then:
        trade.howMuch() == amount
    }

    def "거래는 언제 거래가 이뤄졌는지 알려줍니다" () {
        given:
        LocalDateTime tradeAt = LocalDateTime.of(2023,07,01,14,0)
        Account account = new Account(1L, "00000", TEST_MEMBER, new Bank(1L, "은행", 1L), 1_000L, new HashSet<>(), new HashSet<>())

        when:
        Trade trade = new Trade(1L, TradeType.PAYMENT, tradeAt, 1_000L)

        then:
        trade.when() == tradeAt
    }
}

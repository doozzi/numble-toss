package com.gangbean.stockservice.domain

import spock.lang.Specification

import java.time.LocalDateTime

class AccountStockTradeTest extends Specification {

    def "계좌주식은 같은 ID를 가지면 동등합니다"() {
        given:
        BigDecimal balance = 10
        BigDecimal averagePrice = 5_000
        Long id = 1L
        LocalDateTime tradeAt = LocalDateTime.of(2023, 7, 10, 15, 32, 20)

        when:
        def accountStock = new AccountStockTrade(id, StockTradeType.BUYING, balance, averagePrice, tradeAt)

        then:
        accountStock == new AccountStockTrade(id, null, null, null, null)
    }

    def "계좌주식은 ID를 요청하고, 자신의 ID를 반환합니다"() {
        given:
        BigDecimal balance = 10
        BigDecimal averagePrice = 5_000
        Long id = 1L
        LocalDateTime tradeAt = LocalDateTime.of(2023, 7, 10, 15, 32, 20)

        when:
        def accountStock = new AccountStockTrade(id, StockTradeType.BUYING, balance, averagePrice, tradeAt)

        then:
        accountStock.id() == id
    }

    def "계좌주식은 금액을 요청하고, 자신의 금액을 반환합니다"() {
        given:
        BigDecimal balance = 10
        BigDecimal price = 5_000
        Long id = 1L
        LocalDateTime tradeAt = LocalDateTime.of(2023, 7, 10, 15, 32, 20)

        when:
        def accountStock = new AccountStockTrade(id, StockTradeType.BUYING, balance, price, tradeAt)

        then:
        accountStock.price() == price
    }

    def "계좌주식은 잔량을 요청하고, 자신의 잔량을 반환합니다"() {
        given:
        BigDecimal balance = 10
        BigDecimal averagePrice = 5_000
        Long id = 1L
        LocalDateTime tradeAt = LocalDateTime.of(2023, 7, 10, 15, 32, 20)

        when:
        def accountStock = new AccountStockTrade(id, StockTradeType.BUYING, balance, averagePrice, tradeAt)

        then:
        accountStock.amount() == balance
    }
}

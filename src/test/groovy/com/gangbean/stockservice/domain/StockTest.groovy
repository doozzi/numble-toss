package com.gangbean.stockservice.domain


import spock.lang.Specification

class StockTest extends Specification {

    def "주식은 동일한 ID를 가지면 동등합니다"() {
        given:
        Long stockId = 1L
        String name = "카카오"
        BigDecimal price = 1000L
        BigDecimal balance = 100L
        def stock = new Stock(stockId, name, price, balance, new HashSet<>())

        when:
        String anotherName = "다음"
        BigDecimal anotherPrice = 500L
        BigDecimal anotherBalance = 200L
        def anotherStock = new Stock(stockId, anotherName, anotherPrice, anotherBalance, new HashSet<>())

        then:
        stock == anotherStock
    }

    def "주식은 잔량을 요구하고 본인의 잔량을 알려줍니다"() {
        given:
        Long stockId = 1L
        String name = "카카오"
        BigDecimal price = 1000L
        BigDecimal balance = 100L

        when:
        def stock = new Stock(stockId, name, price, balance, new HashSet<>())

        then:
        stock.howMany() == balance
    }

    def "주식은 가격을 요구하고 본인의 가격을 돌려줍니다"() {
        given:
        Long stockId = 1L
        String name = "카카오"
        BigDecimal price = 1000L

        when:
        def stock = new Stock(stockId, name, price, 100 as BigDecimal, new HashSet<>())

        then:
        stock.howMuch() == price
    }

    def "주식은 이름을 요구하고 본인의 이름을 돌려줍니다"() {
        given:
        Long stockId = 1L
        String name = "카카오"

        when:
        def stock = new Stock(stockId, name, 1000 as BigDecimal, 100 as BigDecimal, new HashSet<>())

        then:
        stock.name() == name
    }

    def "주식은 id를 요구하고 본인의 id를 돌려줍니다"() {
        given:
        Long stockId = 1L

        when:
        def stock = new Stock(stockId, "카카오", 1000 as BigDecimal, 100 as BigDecimal, new HashSet<>())

        then:
        stock.id() == stockId
    }
}

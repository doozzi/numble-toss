package com.gangbean.stockservice.dto

import com.gangbean.stockservice.domain.Stock
import spock.lang.Specification

class StockInfoResponseTest extends Specification {

    def "주식정보응답은 모든 정보가 동일하면 동등합니다"() {
        given:
        def id = 1L
        String stockName = "카카오"
        Long price = 10_000L
        Long balance = 100L
        Stock stock = new Stock(id, stockName, price, balance, new HashSet<>())
        def response = StockInfoResponse.responseOf(stock)

        when:
        def anotherResponse = StockInfoResponse.responseOf(new Stock(id, stockName, price, balance, new HashSet<>()))

        then:
        response == anotherResponse
    }

    def "주식정보응답은 주식을 받아 응답형태로 바꿔줍니다"() {
        given:
        def id = 1L
        String stockName = "카카오"
        Long price = 10_000L
        Long balance = 100L
        Stock stock = new Stock(id, stockName, price, balance, new HashSet<>())

        when:
        def response = StockInfoResponse.responseOf(stock)

        then:
        verifyAll {
            response.getId() == id
            response.getStockName() == stockName
            response.getPrice() == price
            response.getBalance() == balance
        }
    }
}

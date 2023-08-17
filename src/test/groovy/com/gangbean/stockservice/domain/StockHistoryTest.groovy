package com.gangbean.stockservice.domain


import spock.lang.Specification

import java.time.LocalDateTime

class StockHistoryTest extends Specification {
    def "주식이력은 같은 id를 가지면 동등합니다"() {
        given:
        LocalDateTime when = LocalDateTime.of(2023,7,1,14,0)
        Long priorPrice = 900L
        Long historyId = 1L
        def history = new StockHistory(historyId, when, priorPrice)

        when:
        def anotherHistory = new StockHistory(historyId, when, priorPrice)

        then:
        anotherHistory == history
    }

    def "주식이력은 id를 요구하고, 본인이 가진 id를 돌려줍니다"() {
        given:
        LocalDateTime when = LocalDateTime.of(2023,7,1,14,0)
        Long priorPrice = 900L

        Long historyId = 1L

        when:
        def history = new StockHistory(historyId, when, priorPrice)

        then:
        history.id() == historyId
    }

    def "주식이력은 가격을 요구하고, 본인이 가진 가격을 돌려줍니다"() {
        given:
        LocalDateTime when = LocalDateTime.of(2023,7,1,14,0)
        Long priorPrice = 900L

        when:
        def history = new StockHistory(when, priorPrice)

        then:
        history.howMuch() == priorPrice
    }

    def "주식이력은 생성시간을 요구하고, 본인이 가진 생성시간을 돌려줍니다"() {
        given:
        LocalDateTime when = LocalDateTime.of(2023,7,1,14,0)

        when:
        def history = new StockHistory(when, 100L)

        then:
        history.when() == when
    }
}

package com.gangbean.stockservice.repository

import com.gangbean.stockservice.DataIsolationTest
import com.gangbean.stockservice.domain.Stock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@DataIsolationTest
@DataJpaTest
class StockRepositoryTest extends Specification {

    @Autowired
    StockRepository stockRepository

    def "전체 주식이 10개를 초과할때 주식 저장소는 id 기준 10개의 주식을 돌려줍니다"() {
        given:
        def all = stockRepository.findAll()

        when:
        def stocks = stockRepository.findTop10ByOrderById()

        then:
        all.size() > 10
        stocks.size() == 10
    }

    def "주식 저장소는 입력된 id에 해당하는 주식을 돌려줍니다"() {
        given:
        String stockName = "카카오"
        Long price = 10_000L
        Long balance = 100L
        Stock stock = new Stock(stockName, price as BigDecimal, balance as BigDecimal, new HashSet<>())
        def saved = stockRepository.save(stock)

        when:
        def found = stockRepository.findById(saved.id())

        then:
        verifyAll {
            found.isPresent()
            found.get().id() != null
            found.get().name() == stockName
            found.get().howMuch() == price
            found.get().howMany() == balance
        }
    }

    def "주식 저장소는 저장된 주식전체를 돌려줍니다"() {
        String stockName = "카카오"
        Long price = 10_000L
        Long balance = 100L
        Stock stock = new Stock(stockName, price as BigDecimal, balance as BigDecimal, new HashSet<>())
        def saved = stockRepository.save(stock)

        String stockName2 = "네이버"
        Long price2 = 15_000L
        Long balance2 = 150L
        Stock stock2 = new Stock(stockName2, price2 as BigDecimal, balance2 as BigDecimal, new HashSet<>())
        def saved2 = stockRepository.save(stock2)

        when:
        def stocks = stockRepository.findAll()

        then:
        verifyAll {
            stocks.size() == 13
            stocks.containsAll(saved, saved2)
        }
    }

    def "주식 저장소는 주식을 저장하고 저장된 정보를 돌려줍니다"() {
        given:
        String stockName = "카카오"
        Long price = 10_000L
        Long balance = 100L
        Stock stock = new Stock(stockName, price as BigDecimal, balance as BigDecimal, new HashSet<>())

        when:
        def saved = stockRepository.save(stock)

        then:
        verifyAll {
            saved.id() != null
            saved.name() == stockName
            saved.howMuch() == price
            saved.howMany() == balance
        }
    }
}

package com.gangbean.stockservice.service

import com.gangbean.stockservice.domain.Stock
import com.gangbean.stockservice.domain.StockHistory
import com.gangbean.stockservice.dto.StockHistoryInfoResponse
import com.gangbean.stockservice.dto.StockInfoResponse
import com.gangbean.stockservice.repository.StockRepository
import spock.lang.Specification

import java.time.LocalDateTime
import java.util.stream.Collectors

class StockServiceTest extends Specification {

    StockRepository stockRepository

    StockService stockService

    def setup() {
        stockRepository = Mock()
        stockService = new StockService(stockRepository)
    }

    def "주식 서비스는 입력된 id에 해당하는 주식과 해당주식의 이력을 응답형태로 반환해줍니다"() {
        given:
        Long stockId = 1L
        String stockName = "카카오"
        Long price = 10_000L
        Long balance = 100L
        LocalDateTime when = LocalDateTime.of(2023,7,1,14,0)
        Long priorPrice = 900L
        def history = new StockHistory(1L, when, priorPrice)
        def history2 = new StockHistory(2L, when.plusHours(2), 1_200L)
        Stock stock = new Stock(stockId, stockName, price as BigDecimal, balance as BigDecimal, new HashSet<>(Set.of(history, history2)))

        when:
        def response = stockService.responseOfStockDetail(stockId, null)

        then:
        1 * stockRepository.findTop10ByIdOrderByHistoriesWrittenAtDesc(stockId) >> Optional.of(stock)
        def histories = List.of(history, history2).stream().map(StockHistoryInfoResponse::responseOf).collect(Collectors.toList())

        verifyAll {
            response.getStockId() == stockId
            response.getStockName() == stockName
            response.getHistories() == histories
        }
    }

    def "주식 서비스는 주식전체 데이터를 응답형태로 반환해줍니다"() {
        given:
        String stockName = "카카오"
        Long price = 10_000L
        Long balance = 100L
        Stock stock = new Stock(1L, stockName, price as BigDecimal, balance as BigDecimal, new HashSet<>())

        String stockName2 = "네이버"
        Long price2 = 15_000L
        Long balance2 = 150L
        Stock stock2 = new Stock(2L, stockName2, price2 as BigDecimal, balance2 as BigDecimal, new HashSet<>())

        when:
        def allStock = stockService.respondsOfAllStock();

        then:
        1 * stockRepository.findAllByOrderByNameDesc() >> List.of(stock, stock2)
        verifyAll {
            allStock.getStocks().containsAll(
                    StockInfoResponse.responseOf(stock),
                    StockInfoResponse.responseOf(stock2)
            )
        }
    }
}

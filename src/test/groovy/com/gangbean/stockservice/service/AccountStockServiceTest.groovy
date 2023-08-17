package com.gangbean.stockservice.service

import com.gangbean.stockservice.domain.*
import com.gangbean.stockservice.repository.AccountRepository
import com.gangbean.stockservice.repository.AccountStockRepository
import com.gangbean.stockservice.repository.StockRepository
import spock.lang.Specification

import java.time.LocalDateTime

import static com.gangbean.stockservice.domain.MemberTest.TEST_MEMBER

class AccountStockServiceTest extends Specification {

    AccountStockService accountStockService

    AccountRepository accountRepository

    StockRepository stockRepository

    AccountStockRepository accountStockRepository

    def setup() {
        stockRepository = Mock()
        accountRepository = Mock()
        accountStockRepository = Mock()
        accountStockService = new AccountStockService(accountRepository, stockRepository, accountStockRepository)
    }

    def "계좌주식 서비스는 주식판매요청을 받아 주식판매를 진행하고, 기존 구매를 반영한 주식판매결과를 반환해줍니다"() {
        given:
        Long accountId = 1L
        Long stockId = 1L
        String stockName = "카카오"
        BigDecimal price = 10_000L
        BigDecimal balance = 100L
        Stock stock = new Stock(stockId, stockName, price, balance, new HashSet<>())
        BigDecimal sellAmount = 5L
        BigDecimal sellPrice = 9_950L
        LocalDateTime sellAt = LocalDateTime.of(2013, 7, 13, 14, 0)
        BigDecimal boughtAmount = 15L
        BigDecimal boughtPrice = 9_000L
        LocalDateTime boughtAt = LocalDateTime.of(2013, 7, 1, 15, 0)
        def bought = new AccountStockTrade(2L, StockTradeType.SELLING, boughtAmount, boughtPrice, boughtAt)
        def accountStock = new AccountStock(1L, stock, boughtAmount, boughtPrice, boughtAmount * boughtPrice, new HashSet<>(Set.of(bought)))
        Account account = new Account(accountId, "0", TEST_MEMBER, new Bank(1L, "은행", 1L), 1_000_000 as BigDecimal, new HashSet<>(), new HashSet<>(Set.of(accountStock)))

        when:
        def response = accountStockService.responseOfSell(TEST_MEMBER, accountId, stockId, sellAmount, sellPrice, LocalDateTime.now())

        then:
        1 * stockRepository.findById(stockId) >> Optional.of(stock)
        1 * accountRepository.findOneWithMemberAndStocksById(accountId) >> Optional.of(account)

        verifyAll {
            response.getStockId() == stockId
            response.getAmount() == 10
            response.getAveragePrice() == 9_000
            account.balance() == 1_049_750
            stock.howMany() == 105
        }
    }

    def "계좌주식 서비스는 주식구매요청을 받아 주식구매를 진행하고, 기존 구매를 반영한 주식구매결과를 반환해줍니다"() {
        given:
        Long accountId = 1L
        Long stockId = 1L
        String stockName = "카카오"
        BigDecimal price = 10_000L
        BigDecimal balance = 100L
        Stock stock = new Stock(stockId, stockName, price, balance, new HashSet<>())
        BigDecimal newBuyAmount = 10L
        BigDecimal newBuyPrice = 10_050L
        BigDecimal boughtAmount = 5L
        BigDecimal boughtPrice = 9_000L
        LocalDateTime boughtAt = LocalDateTime.of(2013, 7, 1, 15, 0)
        def bought = new AccountStockTrade(1L, StockTradeType.BUYING, boughtAmount, boughtPrice, boughtAt)
        def accountStock = new AccountStock(1L, stock, boughtAmount, boughtPrice, boughtAmount * boughtPrice, new HashSet<>(Set.of(bought)))
        Account account = new Account(accountId, "0", TEST_MEMBER, new Bank(1L, "은행", 1L), 1_000_000 as BigDecimal, new HashSet<>(), new HashSet<>(Set.of(accountStock)))

        when:
        def response = accountStockService.responseOfBuy(TEST_MEMBER, accountId, stockId, newBuyAmount, newBuyPrice, LocalDateTime.now())

        then:
        1 * stockRepository.findById(stockId) >> Optional.of(stock)
        1 * accountRepository.findOneWithMemberAndStocksById(accountId) >> Optional.of(account)

        verifyAll {
            response.getStockId() == stockId
            response.getAmount() == 15
            stock.howMany() == 90
            account.balance() == 899_500
            response.getAveragePrice() == 9_700
        }
    }
}

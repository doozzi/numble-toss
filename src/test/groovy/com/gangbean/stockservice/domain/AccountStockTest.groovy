package com.gangbean.stockservice.domain

import com.gangbean.stockservice.exception.accountstock.AccountStockBuyAmountBelowZeroException
import com.gangbean.stockservice.exception.accountstock.AccountStockBuyPriceBelowZeroException
import com.gangbean.stockservice.exception.accountstock.AccountStockNotEnoughBalanceException
import com.gangbean.stockservice.exception.accountstock.AccountStockSellAmountBelowZeroException
import com.gangbean.stockservice.exception.accountstock.AccountStockSellPriceBelowZeroException
import spock.lang.Specification

import java.time.LocalDateTime

class AccountStockTest extends Specification {

    Account account = new Account(1L, "00001", MemberTest.TEST_MEMBER, new Bank(1L, "은행", 1L), 1000L as BigDecimal, new HashSet<>(), new HashSet<>())
    Stock stock = new Stock(1L, "카카오", 1000L as BigDecimal, 100L as BigDecimal, new HashSet<>())
    AccountStock accountStock
    Long id
    BigDecimal balance
    BigDecimal averagePrice
    BigDecimal total
    Set<AccountStockTrade> stockTrades

    def setup() {
        id = 1L
        balance = 100
        averagePrice = 1_000
        total = 100_000
        stockTrades = new HashSet<>();
        accountStock = new AccountStock(id, stock, balance, averagePrice, total, stockTrades)
    }

    def "계좌주식은 0이하의 금액으로 구매할 수 없습니다"(BigDecimal price) {
        when:
        BigDecimal count = 10
        LocalDateTime tradeAt = LocalDateTime.of(2023, 7, 11, 15, 25, 30)
        accountStock.buy(price, count, tradeAt)

        then:
        def error = thrown(AccountStockBuyPriceBelowZeroException.class)

        expect:
        price <= 0
        error.getMessage() == "0이하의 금액으로 구매할 수 없습니다: " + price

        where:
        price << [0, -101, -1_000, -10_000]
    }

    def "계좌주식은 0이하의 개수는 구매할 수 없습니다"(BigDecimal count) {
        given:
        BigDecimal price = 1_000
        LocalDateTime tradeAt = LocalDateTime.of(2023, 7, 11, 15, 25, 30)

        when:
        accountStock.buy(price, count, tradeAt)

        then:
        def error = thrown(AccountStockBuyAmountBelowZeroException.class)

        expect:
        error.getMessage() == "0이하의 개수는 구매할 수 없습니다: " + count

        where:
        count << [-100, -1, 0]
    }

    def "계좌주식은 [가격, 구매량, 시간]을 입력해, 입력한 가격과 개수만큼 구매해 잔량과 총액을 늘리고, 평균금액을 조정하고, 이력을 남깁니다"() {
        given:
        BigDecimal count = 10
        BigDecimal price = 10_000
        LocalDateTime tradeAt = LocalDateTime.of(2023, 7, 11, 15, 25, 30)

        when:
        accountStock.buy(price, count, tradeAt)

        then:
        verifyAll {
            accountStock.howMany() == 110
            accountStock.howMuchPaid() == 200_000
            accountStock.howMuch() == 1_818
            accountStock.history().stream()
                    .filter(trade -> trade.tradeType() == StockTradeType.BUYING
                            && trade.when() == tradeAt
                            && trade.amount() == 10
                            && trade.price() == 10_000)
                    .findAny()
                    .isPresent()
        }
    }

    def "계좌주식은 보유한 수량을 초과해 팔 수 없습니다"(BigDecimal count) {
        when:
        BigDecimal price = 1000
        LocalDateTime tradeAt = LocalDateTime.of(2023, 7, 11, 15, 25, 30)
        accountStock.sell(price, count, tradeAt)

        then:
        def error = thrown(AccountStockNotEnoughBalanceException.class)

        expect:
        count > accountStock.howMany()
        error.getMessage() == "보유수량이 부족합니다: " + balance

        where:
        count << [101, 1_000, 10_000]
    }

    def "계좌주식은 0이하의 금액으로 팔 수 없습니다"(BigDecimal price) {
        when:
        BigDecimal count = 10
        LocalDateTime tradeAt = LocalDateTime.of(2023, 7, 11, 15, 25, 30)
        accountStock.sell(price, count, tradeAt)

        then:
        def error = thrown(AccountStockSellPriceBelowZeroException.class)

        expect:
        error.getMessage() == "0이하의 금액으로 판매할 수 없습니다: " + price

        where:
        price << [-1000, 0]
    }

    def "계좌주식은 0이하의 개수는 팔 수 없습니다"(BigDecimal count) {
        when:
        BigDecimal price = 1000
        LocalDateTime tradeAt = LocalDateTime.of(2023, 7, 11, 15, 25, 30)
        accountStock.sell(price, count, tradeAt)

        then:
        def error = thrown(AccountStockSellAmountBelowZeroException.class)

        expect:
        error.getMessage() == "0이하의 개수는 판매할 수 없습니다: " + count

        where:
        count << [-1000, 0]
    }

    def "계좌주식은 [가격, 수향, 시간]을 입력해, 입력된 개수만큼 팔아, 보유량과 총액을 줄이고 평균금액은 유지하고, 내역을 생성합니다."() {
        when:
        BigDecimal price = 1000
        BigDecimal sellCount = 10
        LocalDateTime tradeAt = LocalDateTime.of(2023, 7, 11, 15, 25, 30)
        accountStock.sell(price, sellCount, tradeAt)

        then:
        accountStock.howMany() == 90
        accountStock.howMuchPaid() == 90_000
        accountStock.howMuch() == 1_000
        accountStock.history().stream()
                .filter(trade -> trade.tradeType() == StockTradeType.SELLING
                                && trade.when() == tradeAt
                                && trade.amount() == 10
                                && trade.price() == 1000)
                .findAny()
                .isPresent()
    }

    def "계좌주식은 본인의 주식거래내역을 알려줍니다"() {
        when:
        Set<AccountStockTrade> tradeHistory = accountStock.history()

        then:
        tradeHistory == stockTrades
    }

    def "계좌주식은 본인의 평균금액을 알려줍니다"() {
        when:
        BigDecimal howMuch = accountStock.howMuch()

        then:
        howMuch == averagePrice
    }

    def "계좌주식은 본인의 총금액을 알려줍니다"() {
        when:
        Double howMuchTotal = accountStock.howMuchPaid()

        then:
        howMuchTotal == total
    }

    def "계좌주식은 보유량을 요구하고 본인의 보유량을 알려줍니다"() {
        when:
        def howMany = accountStock.howMany()

        then:
        howMany == balance
    }

    def "게좌주식은 주식을 요구하고 본인의 주식을 알려줍니다"() {
        given:
        def accountStockId = 1L

        when:
        def what = accountStock.what()

        then:
        noExceptionThrown()
        what == stock
    }

    def "계좌주식은 id가 같으면 동등합니다"() {
        when:
        def clone = new AccountStock(1L, null, null, null, null, null)

        then:
        noExceptionThrown()
        accountStock == clone
    }

    def "게좌주식은 id를 요구하고 반환합니다"() {
        when:
        def accountStockId = 1L

        then:
        noExceptionThrown()
        accountStock.id() == accountStockId
    }
}

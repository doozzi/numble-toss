package com.gangbean.stockservice.service

import com.gangbean.stockservice.domain.Account
import com.gangbean.stockservice.domain.Bank
import com.gangbean.stockservice.domain.TradeReservation
import com.gangbean.stockservice.repository.AccountRepository
import com.gangbean.stockservice.repository.TradeReservationRepository
import com.gangbean.stockservice.util.BatchExecutionTime
import spock.lang.Specification

import java.time.LocalDateTime

import static com.gangbean.stockservice.domain.MemberTest.TEST_MEMBER

class TradeReservationServiceTest extends Specification {

    TradeReservationService tradeReservationService

    TradeReservationRepository tradeReservationRepository

    AccountRepository accountRepository

    def setup() {
        tradeReservationRepository = Mock()
        accountRepository = Mock()
        tradeReservationService = new TradeReservationService(tradeReservationRepository, accountRepository)
    }

    def "결제예약 서비스는 계좌ID와 예약시간, 금액을 입력하면 결제예약 정보를 만들고 결과를 돌려줍니다"() {
        given:
        Long accountId = 1L
        String number = "000000000"
        String bankName = "은행"
        Long bankNumber = 1L
        Long balance = 1_000_000L
        Account account = new Account(accountId, number, TEST_MEMBER, new Bank(bankName, bankNumber), balance as BigDecimal, new HashSet<>(), new HashSet<>())

        and:
        def amount = 100_000L
        def tradeAt = LocalDateTime.of(2023, 7, 2, 15,0)
        BatchExecutionTime.write("Reservation", tradeAt)
        def reservation = new TradeReservation(1L, account, tradeAt, amount as BigDecimal)

        when:
        def response = tradeReservationService.responseOfPaymentReservation(TEST_MEMBER, accountId, tradeAt, amount as BigDecimal)

        then:
        1 * accountRepository.findById(accountId) >> Optional.of(account)
        1 * tradeReservationRepository.save(_) >> reservation

        verifyAll {
            response.getAccountId() == accountId
            response.getBalance() == balance
            response.getSendAt() == tradeAt
        }
    }

}

package com.gangbean.stockservice.repository

import com.gangbean.stockservice.DataIsolationTest
import com.gangbean.stockservice.domain.Account
import com.gangbean.stockservice.domain.Bank
import com.gangbean.stockservice.domain.TradeReservation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

import java.time.LocalDateTime

import static com.gangbean.stockservice.domain.MemberTest.TEST_MEMBER

@DataIsolationTest
@DataJpaTest
class TradeReservationRepositoryTest extends Specification {

    @Autowired
    TradeReservationRepository tradeReservationRepository

    @Autowired
    BankRepository bankRepository

    @Autowired
    AccountRepository accountRepository

    def "결제예약 리포지토리는 결제예약을 저장합니다"() {
        given:
        def bank = bankRepository.save(new Bank("은행", 1L))
        def account = accountRepository.save(new Account("0", TEST_MEMBER, bank, 10_000L, new HashSet<>(), new HashSet<>()))
        def tradeAt = LocalDateTime.of(2023, 11, 23, 15, 0)
        Long amount = 1_000L

        TradeReservation tradeReservation = new TradeReservation(1L, account, tradeAt, amount)

        when:
        def saved = tradeReservationRepository.save(tradeReservation)

        then:
        verifyAll {
            saved.from() == account
            saved.when() == tradeAt
            saved.howMuch() == amount
        }
    }
}

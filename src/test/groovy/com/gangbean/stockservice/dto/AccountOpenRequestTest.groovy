package com.gangbean.stockservice.dto

import com.gangbean.stockservice.domain.Account
import com.gangbean.stockservice.domain.Bank
import spock.lang.Specification

import static com.gangbean.stockservice.domain.MemberTest.TEST_MEMBER

class AccountOpenRequestTest extends Specification {
    def "계좌저장요청은 계좌를 전달받아 계좌요청저장으로 반환해줍니다" () {
        given:
        Long bankNumber = 1L
        String bankName = "은행"
        String accountNumber = "1"
        Long balance = 100L
        Account account = new Account(accountNumber, TEST_MEMBER, new Bank(bankName, bankNumber), balance, new HashSet<>(), new HashSet<>())

        when:
        def request = AccountOpenRequest.requestOf(account)

        then:
        verifyAll {
            request.bankNumber() == bankNumber
            request.bankName() == bankName
            request.balance() == balance
        }
    }

    def "계좌저장요청은 잔액을 요구하고 알려줍니다"() {
        given:
        Long bankNumber = 1L
        Long balance = 100L

        when:
        def request = new AccountOpenRequest("은행", bankNumber, balance, 1L)

        then:
        request.balance() == balance
    }

    def "계좌저장요청은 은행번호를 요구하고 알려줍니다"() {
        given:
        Long bankNumber = 1L

        when:
        def request = new AccountOpenRequest("은행", bankNumber, 100L, 1L)

        then:
        request.bankNumber() == bankNumber
    }

    def "계좌저장요청은 은행이름을 요구하고 알려줍니다"() {
        given:
        String bankName = "은행"

        when:
        def request = new AccountOpenRequest(bankName, 1L, 100L,1L)

        then:
        request.bankName() == bankName
    }
}

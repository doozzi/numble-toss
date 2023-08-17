package com.gangbean.stockservice.domain


import com.gangbean.stockservice.exception.account.AccountNotEnoughBalanceException
import com.gangbean.stockservice.exception.account.AccountCannotDepositBelowZeroAmountException
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

class AccountTest extends Specification {

    def "게좌는 사용자 정보를 요구하고, 본인의 사용자를 알려줍니다"() {
        Long memberId = 1L
        String userName = "사용자"
        String password = "1234"
        def member = new Member.MemberBuilder().id(memberId)
            .username(userName)
            .password(password)
            .nickname(userName).build()
        Long id = 1L
        String number = "000000000";
        Long bankId = 1L
        String bankName = "은행"
        Long bankNumber = 1L
        Long balance = 1000L

        when:
        Account account = new Account(id, number, member, new Bank(bankId, bankName, bankNumber), balance, new HashSet<>(), new HashSet<>())

        then:
        noExceptionThrown()
        account.whose() == member
    }

    def "계좌는 잔액을 초과하는 금액은 출금할 수 없습니다"(Long amount) {
        Long memberId = 1L
        String userName = "사용자"
        String password = "1234"
        def member = new Member.MemberBuilder().id(memberId)
                .username(userName)
                .password(password)
                .nickname(userName).build()
        Long id = 1L
        String number = "000000000";
        Long bankId = 1L
        String bankName = "은행"
        Long bankNumber = 1L
        Long balance = 1000L
        Account account = new Account(id, number, member, new Bank(bankId, bankName, bankNumber), balance, new HashSet<>(), new HashSet<>())

        when:
        account.withDraw(LocalDateTime.now(), amount)

        then:
        def error = thrown(AccountNotEnoughBalanceException.class)

        expect:
        error.getMessage() == "계좌 잔액이 부족합니다: " + balance

        where:
        amount << [1_001L, 2_000L, 10_000L]
    }

    def "계좌는 입력한 금액만큼 출금할 수 있습니다"() {
        Long memberId = 1L
        String userName = "사용자"
        String password = "1234"
        def member = new Member.MemberBuilder().id(memberId)
                .username(userName)
                .password(password)
                .nickname(userName).build()
        Long id = 1L
        String number = "000000000";
        Long bankId = 1L
        String bankName = "은행"
        Long bankNumber = 1L
        Long balance = 1000L
        Account account = new Account(id, number, member, new Bank(bankId, bankName, bankNumber), balance, new HashSet<>(), new HashSet<>())

        Long amount = 1000L

        when:
        account.withDraw(LocalDateTime.now(), amount)

        then:
        account.balance() == 0
    }

    @Unroll
    def "계좌는 0원 이하 금액을 입금할 수 있습니다"(Long amount) {
        Long memberId = 1L
        String userName = "사용자"
        String password = "1234"
        def member = new Member.MemberBuilder().id(memberId)
                .username(userName)
                .password(password)
                .nickname(userName).build()
        Long id = 1L
        String number = "000000000";
        Long bankId = 1L
        String bankName = "은행"
        Long bankNumber = 1L
        Long balance = 1_000L
        Account account = new Account(id, number, member, new Bank(bankId, bankName, bankNumber), balance, new HashSet<>(), new HashSet<>())

        when:
        account.deposit(LocalDateTime.now(), amount)

        then:
        def error = thrown(AccountCannotDepositBelowZeroAmountException.class)

        expect:
        error.getMessage() == "계좌는 0원 이하 금액을 입금할 수 없습니다: " + amount

        where:
        amount << [0L, -100L]
    }

    def "계좌는 입력한 금액만큼 입금할 수 있습니다"() {
        Long memberId = 1L
        String userName = "사용자"
        String password = "1234"
        def member = new Member.MemberBuilder().id(memberId)
                .username(userName)
                .password(password)
                .nickname(userName).build()
        Long id = 1L
        String number = "000000000";
        Long bankId = 1L
        String bankName = "은행"
        Long bankNumber = 1L
        Long balance = 1_000L
        Account account = new Account(id, number, member, new Bank(bankId, bankName, bankNumber), balance, new HashSet<>(), new HashSet<>())

        Long amount = 1_000L

        when:
        account.deposit(LocalDateTime.now(), amount as BigDecimal)

        then:
        account.balance() == 2_000L
    }

    def "계좌는 ID와 계좌번호, 은행, 잔액을 알려줍니다"() {
        given:
        Long memberId = 1L
        String userName = "사용자"
        String password = "1234"
        def member = new Member.MemberBuilder().id(memberId)
                .username(userName)
                .password(password)
                .nickname(userName).build()
        Long id = 1L
        String number = "000000000";
        Long bankId = 1L
        String bankName = "은행"
        Long bankNumber = 1L
        Long balance = 1000L

        when:
        Account account = new Account(id, number, member, new Bank(bankId, bankName, bankNumber), balance, new HashSet<>(), new HashSet<>())

        then:
        verifyAll {
            account.id() == id
            account.number() == number
            account.bank() == new Bank(bankId, bankName, bankNumber)
            account.balance() == balance
        }
    }

    def "계좌는 계좌번호와 은행, 잔액을 요구합니다"() {
        given:
        Long memberId = 1L
        String userName = "사용자"
        String password = "1234"
        def member = new Member.MemberBuilder().id(memberId)
                .username(userName)
                .password(password)
                .nickname(userName).build()
        String number = "0000000000"
        Long bankId = 1L
        String bankName = "XX은행"
        Long bankNumber = 1L
        Long balance = 1000L

        when:
        new Account(number, member, new Bank(bankId, bankName, bankNumber), balance, new HashSet<>(), new HashSet<>())

        then:
        noExceptionThrown()
    }
}

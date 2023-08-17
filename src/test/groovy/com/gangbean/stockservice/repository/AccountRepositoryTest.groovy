package com.gangbean.stockservice.repository

import com.gangbean.stockservice.DataIsolationTest
import com.gangbean.stockservice.domain.Account
import com.gangbean.stockservice.domain.Bank
import com.gangbean.stockservice.domain.Trade
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

import static com.gangbean.stockservice.domain.MemberTest.TEST_MEMBER

@DataIsolationTest
@DataJpaTest
class AccountRepositoryTest extends Specification {

    @Autowired
    private AccountRepository accountRepository

    @Autowired
    private BankRepository bankRepository

    String bankName = "은행"
    Long bankNumber = 1L
    Bank bank
    Long bankId

    def setup() {
        bank = bankRepository.save(new Bank(bankName, bankNumber))
        bankId = bank.id()
    }

    def cleanup() {
        bankRepository.deleteAll()
    }

    def "계좌 리포지토리는 전체 계좌의 목록을 반환합니다"() {
        given:
        String number = "000000004"
        String number2 = "000000005"
        Long balance = 1000L
        def saved = accountRepository.save(new Account(number, TEST_MEMBER, bank, balance as BigDecimal, new HashSet<>(), new HashSet<>()))
        def saved2 = accountRepository.save(new Account(number2, TEST_MEMBER, bank, balance as BigDecimal, new HashSet<>(), new HashSet<>()))

        when:
        def list = accountRepository.findAll()

        then:
        verifyAll {
            list.size() == 4
            list.containsAll(saved, saved2)
        }
    }

    def "게좌 리포지토리는 입력된 id에 해당하는 계좌정보를 반환합니다"() {
        given:
        String number = "000000003";
        Long balance = 1000L
        Account account = new Account(number, TEST_MEMBER, bank, balance as BigDecimal, new HashSet<>() as Set<Trade>, new HashSet<>())
        def saved = accountRepository.save(account)

        when:
        def find = accountRepository.findById(saved.id())

        then:
        verifyAll {
            find.isPresent()
            find.get() == saved
            find.get().number() == saved.number()
            find.get().balance() == saved.balance()
        }
    }

    def "계좌 리포지토리는 입력한 계좌정보를 삭제합니다"() {
        given:
        String number = "000000002";
        Long balance = 1000L
        Account account = new Account(number, TEST_MEMBER, bank, balance as BigDecimal, new HashSet<>() as Set<Trade>, new HashSet<>())
        def saved = accountRepository.save(account)

        when:
        accountRepository.delete(saved)

        then:
        noExceptionThrown()
    }

    def "계좌 리포지토리는 계좌정보를 저장합니다"() {
        given:
        String number = "000000001";
        Long balance = 1000L
        Account account = new Account(number, TEST_MEMBER, bank, balance, new HashSet<>(), new HashSet<>())

        when:
        def saved = accountRepository.save(account)

        then:
        verifyAll {
            saved.id() != null
            saved.number() == number
            saved.bank() == new Bank(bankId, bankName, bankNumber)
            saved.balance() == balance
        }
    }
}

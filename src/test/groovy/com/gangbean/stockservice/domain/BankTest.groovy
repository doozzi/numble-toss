package com.gangbean.stockservice.domain

import com.gangbean.stockservice.domain.Bank
import spock.lang.Specification

class BankTest extends Specification {

    def "은행은 본인의 id, 이름, 번호를 알려줍니다"() {
        given:
        Long id = 1L
        String name = "우리은행"
        Long number = 1L
        def bank = new Bank(id, name, number)

        expect:
        verifyAll {
            id == bank.id()
            name == bank.name()
            number == bank.number()
        }
    }

    def "은행은 같은 id를 가지면 동일한 은행입니다"() {
        given:
        Long id = 1L

        when:
        def bank = new Bank(id)

        then:
        verifyAll {
            bank == new Bank(id)
            bank != new Bank(2L)
        }
    }

    def "은행은 은행이름과 은행번호를 요구합니다"() {
        given:
        String bankName = "우리은행"
        Long bankNumber = 1L

        when:
        new Bank(bankName, bankNumber)

        then:
        noExceptionThrown()
    }
}

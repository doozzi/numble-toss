package com.gangbean.stockservice.service

import com.gangbean.stockservice.dto.BankInfoResponse
import com.gangbean.stockservice.domain.Bank
import com.gangbean.stockservice.exception.account.BankNotFoundException
import com.gangbean.stockservice.repository.BankRepository
import spock.lang.Specification

class BankServiceTest extends Specification {

    BankService bankService
    BankRepository bankRepository

    def setup() {
        bankRepository = Mock()
        bankService = new BankService(bankRepository)
    }

    def "은행 서비스는 은행 이름과 번호에 해당하는 은행이 존재하지 않으면 오류를 반환합니다"() {
        given:
        def name = "없는은행"
        def number = 0L

        when:
        bankService.existingBank(name, number)

        then:
        1 * bankRepository.findByNameAndNumber(name, number) >> Optional.empty()
        def throwable = thrown(BankNotFoundException.class)
        throwable.getMessage() == "은행 이름과 번호에 해당하는 은행이 존재하지 않습니다: " + name + " / " + number
    }

    def "은행 서비스는 은행 이름과 번호에 해당하는 은행을 반환해줍니다"() {
        given:
        String name = "우리은행"
        Long number = 1L
        Long id = 1L
        def bank = new Bank(id, name, number)

        when:
        BankInfoResponse response = bankService.existingBank(name, number)

        then:
        1 * bankRepository.findByNameAndNumber(name, number) >> Optional.of(bank)
        verifyAll {
            response.id() == id
            response.name() == name
            response.number() == number
        }
    }
}

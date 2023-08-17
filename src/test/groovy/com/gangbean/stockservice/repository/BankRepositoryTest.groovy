package com.gangbean.stockservice.repository

import com.gangbean.stockservice.DataIsolationTest
import com.gangbean.stockservice.domain.Bank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@DataIsolationTest
@DataJpaTest
class BankRepositoryTest extends Specification {

    @Autowired
    BankRepository bankRepository;

    def "은행 리포지토리는 저장된 은행의 정보를 이름과 번호의 조합으로 조회할 수 있습니다"() {
        given:
        String name = "우리은행"
        Long number = 1L
        def bank = new Bank(name, number)
        def save = bankRepository.save(bank)

        when:
        def findByNameAndNumber = bankRepository.findByNameAndNumber(name, number)

        then:
        verifyAll {
            findByNameAndNumber.isPresent()
            findByNameAndNumber.get().name() == name
            findByNameAndNumber.get().number() == number
        }
    }

    def "은행 리포지토리는 저장된 은행의 정보를 id 를 통해 조회할 수 있습니다"() {
        given:
        String name = "우리은행"
        Long number = 1L
        def bank = new Bank(name, number)
        def save = bankRepository.save(bank)

        when:
        def findById = bankRepository.findById(save.id())

        then:
        verifyAll {
            findById.isPresent()
            findById.get().name() == name
            findById.get().number() == number
        }
    }

    def "은행 리포지토리는 은행 정보를 저장합니다"() {
        given:
        String name = "우리은행"
        Long number = 1L
        def bank = new Bank(name, number)

        when:
        def save = bankRepository.save(bank)

        then:
        verifyAll {
            name == bank.name()
            number == bank.number()
        }
    }
}

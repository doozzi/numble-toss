package com.gangbean.stockservice.domain

import com.gangbean.stockservice.domain.TradeType
import spock.lang.Specification

class TradeTypeTest extends Specification {

    def "거래정보는 결제를 포함합니다"() {
        when:
        TradeType.values().contains(TradeType.PAYMENT)

        then:
        noExceptionThrown()
    }
    def "거래정보는 출금을 포함합니다"() {
        when:
        TradeType.values().contains(TradeType.WITHDRAW)

        then:
        noExceptionThrown()
    }

    def "거래정보는 입금을 포함합니다"() {
        when:
        TradeType.values().contains(TradeType.DEPOSIT)

        then:
        noExceptionThrown()
    }

}

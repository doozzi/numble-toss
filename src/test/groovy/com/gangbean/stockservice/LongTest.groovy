package com.gangbean.stockservice

import spock.lang.Specification

class LongTest extends Specification {

    def "toSting 은 값 숫자를 스트링을 반환해줍니다"() {
        given:
        Long id = 1L

        when:
        String toString = id.toString()

        then:
        toString == "1"
    }
}

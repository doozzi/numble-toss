package com.gangbean.stockservice

import spock.lang.Specification

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class LocalDateTimeTest extends Specification {

    def "truncate" (int hour) {
        when:
        def time = LocalDateTime.of(2023, 7, 1, hour, 1)

        then:
        time.truncatedTo(ChronoUnit.HOURS) != time

        where:
        hour << [1,2,3,4,5,6,7,23]
    }

    def "LocalDateTime 은 당일 0시 0분으로 변환가능하다" (int hour) {
        when:
        def time = LocalDateTime.of(2023, 7, 1, hour, 1)

        then:
        time.toLocalDate().atStartOfDay().plusHours(hour) != time

        where:
        hour << [1,2,3,4,5,6,7,23]
    }
}

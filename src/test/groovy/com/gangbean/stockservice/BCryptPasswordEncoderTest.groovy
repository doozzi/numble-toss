package com.gangbean.stockservice

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import spock.lang.Specification

class BCryptPasswordEncoderTest extends Specification {

    def encoder = new BCryptPasswordEncoder()

    def "패스워드 인코더는 평문을 암호문으로 바꿔줍니다"() {
        given:
        String plain = "admin"
        String encodedPrevious = "\$2a\$08\$lDnHPz7eUkSi6ao14Twuau08mzhWrL4kyZGGU5xfiGALO/Vxd5DOi"

        when:
        String encoding = encoder.encode(plain)

        then:
        encoder.matches(plain, encoding)
        encoder.matches(plain, encodedPrevious)
    }
}

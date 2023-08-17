package com.gangbean.stockservice.dto

import com.gangbean.stockservice.domain.Account
import com.gangbean.stockservice.domain.Bank
import spock.lang.Specification

import static com.gangbean.stockservice.domain.MemberTest.TEST_MEMBER

class AccountInfoListResponseTest extends Specification {
    def "계좌 목록 조회 응답은 계좌정보의 목록을 알려줍니다"() {
        given:
        def id = 1L
        def number = "0000000000"
        def balance = 1_000L
        def bankName = "은행"
        def bankNumber = 1L
        List<AccountInfoResponse> list = new ArrayList<>()
        list.add(AccountInfoResponse.responseOf(new Account(id, number,TEST_MEMBER,  new Bank(bankName, bankNumber), balance, new HashSet<>(), new HashSet<>())))
        def response = new AccountInfoListResponse(list)

        when:
        List<AccountInfoResponse> accountInfoList = response.accountInfoList()

        then:
        verifyAll {
            accountInfoList.size() == 1
            accountInfoList.get(0).id() == id
            accountInfoList.get(0).bankName() == bankName
            accountInfoList.get(0).bankNumber() == bankNumber
            accountInfoList.get(0).balance() == balance
        }
    }

    def "계좌 목록 조회 응답은 계좌정보의 목록을 요구합니다"() {
        given:
        def id = 1L
        def number = "0000000000"
        def balance = 1_000L
        def bankName = "은행"
        def bankNumber = 1L
        List<AccountInfoResponse> list = new ArrayList<>()
        list.add(AccountInfoResponse.responseOf(new Account(id, number, TEST_MEMBER, new Bank(bankName, bankNumber), balance, new HashSet<>(), new HashSet<>())))

        when:
        new AccountInfoListResponse(list)

        then:
        noExceptionThrown()
    }
}

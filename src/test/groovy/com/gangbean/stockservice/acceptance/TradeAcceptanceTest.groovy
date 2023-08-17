package com.gangbean.stockservice.acceptance

import com.gangbean.stockservice.SpringBootAcceptanceTest
import com.gangbean.stockservice.repository.AccountRepository
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.specification.RequestSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.restdocs.ManualRestDocumentation
import spock.lang.Specification

import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration

@SpringBootAcceptanceTest
class TradeAcceptanceTest extends Specification {

    @LocalServerPort
    int port

    @Autowired
    AccountRepository accountRepository

    String token

    String username

    String password

    public ManualRestDocumentation restDocumentation = new ManualRestDocumentation()

    private RequestSpecification spec

    def setup() {
        RestAssured.port = port
        username = "admin"
        password = "admin"
        def loginResponse = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("username", username, "password", password))
                .when()
                .post("/api/login")
                .then().log().all()
                .extract()
        token = loginResponse.header("Authorization")
        this.spec = new RequestSpecBuilder().addFilter(
                documentationConfiguration(this.restDocumentation))
                .build()
        this.restDocumentation.beforeTest(getClass(), "transferOk");

    }

    /**
     * given 로그인한 상태로 계좌ID, 수신계좌번호, 금액을 입력하고
     * and 해당하는 계좌가 존재하고
     * and 계좌의 주인이고
     * and 수신계좌번호에 해당하는 계좌가 존재하고
     * and 송금계좌와 수신계좌가 동일하지 않고
     * and 금액이 0 초과이면
     * when 송금요청시
     * then 201 Created 응답이 반환됩니다.
     */
    def transferOk() {
        given:
        def accountId = 1L
        def accountNumber = "1235"
        def amount = 100L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        def receiveAccount = accountRepository.findByNumber(accountNumber)
        assert receiveAccount.isPresent()

        and:
        assert receiveAccount.get() != account.get()

        and:
        assert amount > 0

        when:
        def response = RestAssured.given(this.spec).log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .filter(document("stock-selling",
                        preprocessRequest(modifyUris()
                                .scheme("https")
                                .host("ec2-43-201-193-154.ap-northeast-2.compute.amazonaws.com")
                                .removePort(),
                                prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("계좌ID")
                        ),
                        requestFields(
                                fieldWithPath("amount").description("송금요청금액"),
                                fieldWithPath("receiverAccountNumber").description("요청 수신계좌번호")
                        ),
                        responseFields(
                                fieldWithPath("balance").description("계좌잔약")
                        )
                ))
                .body(Map.of("receiverAccountNumber", accountNumber, "amount", amount))
                .when()
                .post("/api/accounts/{id}/trades", accountId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.CREATED.value()
            accountRepository.findById(account.get().id()).get().balance() == 900
            accountRepository.findById(receiveAccount.get().id()).get().balance() == 1_100L
        }
    }

    /**
     * given 로그인한 상태로 계좌ID, 수신계좌번호, 금액을 입력하고
     * and 해당하는 계좌가 존재하고
     * and 계좌의 주인이고
     * and 수신계좌번호에 해당하는 계좌가 존재하고
     * and 송금계좌와 수신계좌가 동일하지 않고
     * and 금액이 0 초과이고
     * and 송금게좌잔액이 금액보다 적으면
     * when 송금요청시
     * then 406 Not Acceptable 응답이 반환됩니다.
     */
    def transferNotEnoughBalance() {
        given:
        def accountId = 1L
        def accountNumber = "1235"
        def amount = 10_000_000L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        def receiveAccount = accountRepository.findByNumber(accountNumber)
        assert receiveAccount.isPresent()

        and:
        assert receiveAccount.get() != account.get()

        and:
        assert amount > 0

        and:
        assert account.get().balance() < amount

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("receiverAccountNumber", accountNumber, "amount", amount))
                .when()
                .post("/api/accounts/{id}/trades", accountId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.NOT_ACCEPTABLE.value()
            response.jsonPath().getString("message") == "계좌 잔액이 부족합니다: " + account.get().balance()
        }
    }

    /**
     * given 로그인한 상태로 계좌ID, 수신계좌번호, 금액을 입력하고
     * and 해당하는 계좌가 존재하고
     * and 계좌의 주인이고
     * and 수신계좌번호에 해당하는 계좌가 존재하고
     * and 송금계좌와 수신계좌가 동일하지 않고
     * and 금액이 0이하이면
     * when 송금요청시
     * then 401 Bad Request 응답이 반환됩니다.
     */
    def transferBelowZero(Long amount) {
        given:
        def accountId = 1L
        def accountNumber = "1235"

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        def receiveAccount = accountRepository.findByNumber(accountNumber)
        assert receiveAccount.isPresent()

        and:
        assert receiveAccount.get() != account.get()

        and:
        assert amount <= 0

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("receiverAccountNumber", accountNumber, "amount", amount))
                .when()
                .post("/api/accounts/{id}/trades", accountId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.BAD_REQUEST.value()
            response.jsonPath().getString("message") == "계좌는 0원 이하의 금액은 송금할 수 없습니다: " + amount
        }

        where:
        amount << [-100L, 0L]
    }

    /**
     * given 로그인한 상태로 계좌ID, 수신계좌번호, 금액을 입력하고
     * and 해당하는 계좌가 존재하고
     * and 계좌의 주인이고
     * and 수신계좌번호에 해당하는 계좌가 존재하고
     * and 송금계좌와 수신계좌가 동일하면
     * when 송금요청시
     * then 406 Not Acceptable 응답이 반환됩니다.
     */
    def transferSameAccounts() {
        given:
        def accountId = 1L
        def accountNumber = "1234"
        def amount = 100L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        def receiveAccount = accountRepository.findByNumber(accountNumber)
        assert receiveAccount.isPresent()

        and:
        assert receiveAccount.get() == account.get()

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("receiverAccountNumber", accountNumber, "amount", amount))
                .when()
                .post("/api/accounts/{id}/trades", accountId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.NOT_ACCEPTABLE.value()
            response.jsonPath().getString("message") == "송금계좌와 수신계좌가 동일할 수 없습니다."
        }
    }

    /**
     * given 로그인한 상태로 계좌ID, 수신계좌번호, 금액을 입력하고
     * and 해당하는 계좌가 존재하고
     * and 계좌의 주인이고
     * and 수신계좌번호에 해당하는 계좌가 존재하지 않으면
     * when 송금요청시
     * then 404 Not Found 응답이 반환됩니다.
     */
    def transferNoAccountNumber() {
        given:
        def accountId = 1L
        def accountNumber = "2000"
        def amount = 100L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        def receiveAccount = accountRepository.findByNumber(accountNumber)
        assert receiveAccount.isEmpty()

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("receiverAccountNumber", accountNumber, "amount", amount))
                .when()
                .post("/api/accounts/{id}/trades", accountId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.NOT_FOUND.value()
            response.jsonPath().getString("message") == "입력된 계좌번호에 해당하는 계좌가 존재하지 않습니다: " + accountNumber
        }
    }

    /**
     * given 로그인한 상태로 계좌ID, 수신계좌번호, 금액을 입력하고
     * and 해당하는 계좌가 존재하고
     * and 계좌의 주인이 아니면
     * when 송금요청시
     * then 403 Forbidden 응답이 반환됩니다.
     */
    def transferNotOwnedAccount() {
        given:
        def accountId = 2L
        def accountNumber = "1235"
        def amount = 100L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() != username

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("receiverAccountNumber", accountNumber, "amount", amount))
                .when()
                .post("/api/accounts/{id}/trades", accountId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.FORBIDDEN.value()
            response.jsonPath().getString("message") == "본인의 계좌가 아닙니다: " + accountId
        }
    }

    /**
     * given 로그인한 상태로 계좌ID, 수신계좌번호, 금액을 입력하고
     * and 해당하는 계좌가 존재하지 않으면
     * when 송금요청시
     * then 404 Not Found 응답이 반환됩니다.
     */
    def transferNoAccount() {
        given:
        def accountId = 10L
        def accountNumber = "1235"
        def amount = 100L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isEmpty()

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("receiverAccountNumber", accountNumber, "amount", amount))
                .when()
                .post("/api/accounts/{id}/trades", accountId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.NOT_FOUND.value()
            response.jsonPath().getString("message") == "입력된 ID에 해당하는 계좌가 존재하지 않습니다: " + accountId
        }
    }

}

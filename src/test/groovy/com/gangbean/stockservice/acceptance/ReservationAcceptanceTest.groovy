package com.gangbean.stockservice.acceptance

import com.gangbean.stockservice.SpringBootAcceptanceTest
import com.gangbean.stockservice.jwt.TokenProvider
import com.gangbean.stockservice.repository.AccountRepository
import com.gangbean.stockservice.util.BatchExecutionTime
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.specification.RequestSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.restdocs.ManualRestDocumentation
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration

@SpringBootAcceptanceTest
class ReservationAcceptanceTest extends Specification {

    @LocalServerPort
    int port

    @Autowired
    AccountRepository accountRepository

    @Autowired
    TokenProvider tokenProvider

    String token

    String username

    String password

    Long accountId

    BigDecimal amount

    LocalDateTime reserveAt

    LocalDateTime nextExecuteTime

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
        accountId = 1L
        amount = 100
        reserveAt = LocalDateTime.of(2023, 7, 12, 20, 0, 0)
        nextExecuteTime = LocalDateTime.of(2023, 7, 12, 19, 0, 0)
        BatchExecutionTime.write("Reservation", nextExecuteTime)
        this.spec = new RequestSpecBuilder().addFilter(
                documentationConfiguration(this.restDocumentation))
                .build()
        this.restDocumentation.beforeTest(getClass(), "예약결제_정상");
    }

    /**
     * given 로그인한 상태로 결제계좌 ID와 금액, 예약시간을 보내고
     * and 결제게좌가 존재하고
     * and 본인의 계좌이고
     * and 금액이 0원보다 크고
     * and 계좌잔액이 충분하고
     * and 에약시간이 기준시간 이후이고
     * and 예약시간이 정각일때
     * when 예약결제요청시
     * then 201 Created 응답과 계좌 ID, 잔액, 예약시간이 반환됩니다.
     */
    def "예약결제_정상"() {
        given:
        accountId = 1L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        assert account.get().balance() >= amount

        and:
        assert reserveAt.isAfter(nextExecuteTime)

        and:
        assert reserveAt.truncatedTo(ChronoUnit.HOURS) == reserveAt

        when:
        def response = RestAssured.given(this.spec).log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .filter(document("reservation",
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
                                fieldWithPath("amount").description("결제요청금액"),
                                fieldWithPath("sendAt").description("결제예약 요청일시")
                        ),
                        responseFields(
                                fieldWithPath("accountId").description("계좌ID"),
                                fieldWithPath("balance").description("계좌잔액"),
                                fieldWithPath("sendAt").description("결제예약 확정일시")
                        )
                ))
                .header("Authorization", token)
                .body(Map.of("amount", amount, "sendAt", reserveAt))
                .when()
                .post("/api/accounts/{id}/reservations", accountId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.CREATED.value()
            response.jsonPath().getLong("accountId") == 1L
            new BigDecimal(response.jsonPath().getString("balance")) == 1_000
            LocalDateTime.parse(response.jsonPath().getString("sendAt")) == reserveAt
        }
    }

    /**
     * given 로그인한 상태로 결제계좌 ID와 금액, 예약시간을 보내고
     * and 결제게좌가 존재하고
     * and 본인의 계좌이고
     * and 금액이 0원보다 크고
     * and 계좌잔액이 충분하고
     * and 예약시간이 기준시간 이후이고
     * and 에약시간이 정각이 아닐때
     * when 예약결제요청시
     * then 400 Bad request 응답이 반환됩니다.
     */
    def "예약결제_예약시간정각아님"() {
        given:
        accountId = 1L
        reserveAt = LocalDateTime.of(2023, 7, 12, 20, 10, 0)

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        assert amount > 0

        and:
        assert account.get().balance() >= amount

        and:
        assert reserveAt.isAfter(nextExecuteTime) || reserveAt.isEqual(nextExecuteTime)

        and:
        assert reserveAt != reserveAt.truncatedTo(ChronoUnit.HOURS)

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("amount", amount, "sendAt", reserveAt))
                .when()
                .post("/api/accounts/{id}/reservations", accountId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.BAD_REQUEST.value()
            response.jsonPath().getString("message") == "결제예약은 매 시간단위로만 요청가능합니다: " + reserveAt
        }
    }

    /**
     * given 로그인한 상태로 결제계좌 ID와 금액, 예약시간을 보내고
     * and 결제게좌가 존재하고
     * and 본인의 계좌이고
     * and 금액이 0원보다 크고
     * and 계좌잔액이 충분하고
     * and 예약시간이 기준시간 이전일때
     * when 예약결제요청시
     * then 400 Bad request 응답이 반환됩니다.
     */
    def "예약결제_예약시간지남"() {
        given:
        accountId = 1L
        reserveAt = LocalDateTime.of(2022, 7, 12, 20, 0, 0)

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        assert amount > 0

        and:
        assert account.get().balance() >= amount

        and:
        assert reserveAt.isBefore(nextExecuteTime)

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("amount", amount, "sendAt", reserveAt))
                .when()
                .post("/api/accounts/{id}/reservations", accountId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.BAD_REQUEST.value()
            response.jsonPath().getString("message") == "다음 수행 예정시각 이전으로 예약할 수 없습니다: " + nextExecuteTime
        }
    }

    /**
     * given 로그인한 상태로 결제계좌 ID와 금액, 예약시간을 보내고
     * and 결제게좌가 존재하고
     * and 본인의 계좌이고
     * and 요청금액이 0원보다 크고
     * and 계좌잔액이 부족할 때
     * when 예약결제요청시
     * then 400 Bad request 오류가 반환됩니다.
     */
    def "예약결제_잔액부족"() {
        given:
        accountId = 1L
        amount = 1_000_000

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        assert amount > 0

        and:
        assert account.get().balance() < amount

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("amount", amount, "sendAt", reserveAt))
                .when()
                .post("/api/accounts/{id}/reservations", accountId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.BAD_REQUEST.value()
            response.jsonPath().getString("message") == "계좌의 잔액이 부족합니다: " + account.get().balance()
        }
    }

    /**
     * given 로그인한 상태로 결제계좌 ID와 금액, 예약시간을 보내고
     * and 결제게좌가 존재하고
     * and 본인의 계좌이고
     * and 결제요청금액이 0원이하이면
     * when 예약결제요청시
     * then 400 Bad request 오류가 반환됩니다.
     */
    def "예약결제_0원이하요청"(BigDecimal amount) {
        given:
        accountId = 1L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        assert amount <= 0

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("amount", amount, "sendAt", reserveAt))
                .when()
                .post("/api/accounts/{id}/reservations", accountId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.BAD_REQUEST.value()
            response.jsonPath().getString("message") == "0이하의 금액은 예약불가합니다: " + amount
        }

        where:
        amount << [-100, -10, 0]
    }

    /**
     * given 로그인한 상태로 결제계좌 ID와 금액, 예약시간을 보내고
     * and 결제게좌가 존재하고
     * and 본인의 계좌가 아닐때
     * when 예약결제요청시
     * then 403 Forbidden 오류가 반환됩니다.
     */
    def "예약결제_타인게좌"() {
        given:
        accountId = 2L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() != username

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("amount", amount, "sendAt", reserveAt))
                .when()
                .post("/api/accounts/{id}/reservations", accountId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.FORBIDDEN.value()
            response.jsonPath().getString("message") == "본인의 계좌가 아닙니다: " + accountId
        }
    }

    /**
     * given 로그인한 상태로 결제계좌 ID와 금액, 예약시간을 보내고
     * and 결제게좌가 존재하지 않으면
     * when 예약결제요청시
     * then 404 Not found 오류가 반환됩니다.
     */
    def "예약결제_미존재게좌"() {
        given:
        accountId = 101L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isEmpty()

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("amount", amount, "sendAt", reserveAt))
                .when()
                .post("/api/accounts/{id}/reservations", accountId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.NOT_FOUND.value()
            response.jsonPath().getString("message") == "입력된 ID에 해당하는 계좌가 존재하지 않습니다: " + accountId
        }
    }

}

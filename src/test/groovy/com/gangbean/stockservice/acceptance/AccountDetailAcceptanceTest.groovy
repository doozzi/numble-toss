package com.gangbean.stockservice.acceptance

import com.gangbean.stockservice.SpringBootAcceptanceTest
import com.gangbean.stockservice.jwt.TokenProvider
import com.gangbean.stockservice.repository.AccountRepository
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.specification.RequestSpecification
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.payload.JsonFieldType
import spock.lang.Specification

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration

@SpringBootAcceptanceTest
class AccountDetailAcceptanceTest extends Specification {
    @LocalServerPort
    int port

    @Autowired
    AccountRepository accountRepository

    @Autowired
    TokenProvider tokenProvider

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
        this.restDocumentation.beforeTest(getClass(), "계좌상세조회_정상");
    }

    /**
     * given 로그인한 상태로 계좌 ID를 입력하고
     * and 해당하는 계좌가 존재하지 않을때
     * when 계좌 상세 조회 요청을 하면
     * then 404 Not found 응답이 반환됩니다.
     */
    def "계좌상세조회_미존재계좌"() {
        given:
        def accountId = 3L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isEmpty()

        when:
        def response = RestAssured.given().log().all()
                .header("Authorization", token)
                .when()
                .get("/api/accounts/{id}", accountId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.NOT_FOUND.value()
            response.jsonPath().getString("message") == "입력된 ID에 해당하는 계좌가 존재하지 않습니다: " + accountId
        }
    }

    /**
     * given 로그인한 상태로 계좌 ID를 입력하고
     * and 해당하는 계좌가 존재하고
     * and 로그인한 사용자가 계좌 주인이 아닐 때
     * when 계좌 상세 조회 요청을 하면
     * then 403 Forbidden 응답이 반환됩나다.
     */
    def "계좌상세조회_타인계좌"() {
        given:
        def accountId = 2L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() != username

        when:
        def response = RestAssured.given().log().all()
                .header("Authorization", token)
                .when()
                .get("/api/accounts/{id}", accountId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.FORBIDDEN.value()
            response.jsonPath().getString("message") == "본인의 계좌가 아닙니다: " + accountId
        }
    }

    /**
     * given 로그인한 상태로 계좌 ID를 입력하고
     * and 로그인한 사용자가 게좌 주인일 때
     * when 계좌 상세 조회 요청을 하면
     * then 200 Ok 응답과 계좌 거래내역을 포함한 상세내역이 반환됩니다.
     */
    def "계좌상세조회_정상"() {
        given:
        def accountId = 1L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        when:
        def response = RestAssured.given(this.spec).log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .filter(document("get-account-detail",
                        preprocessRequest(modifyUris()
                                .scheme("https")
                                .host("ec2-43-201-193-154.ap-northeast-2.compute.amazonaws.com")
                                .removePort(),
                                prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("계좌ID")
                        ),
                        responseFields(
                                fieldWithPath("id").description("게좌ID"),
                                fieldWithPath("accountNumber").description("게좌번호"),
                                fieldWithPath("bankName").description("은행명"),
                                fieldWithPath("bankNumber").description("은행번호"),
                                fieldWithPath("balance").description("게좌잔액"),

                                fieldWithPath("trades[]").description("계좌거래내역"),
                                fieldWithPath("trades[].id").description("계좌거래ID"),
                                fieldWithPath("trades[].tradeType").description("거래종류"),
                                fieldWithPath("trades[].tradeAt").description("거래일"),
                                fieldWithPath("trades[].amount").description("거래금액")
                        )
                ))
                .header("Authorization", token)
                .when()
                .get("/api/accounts/{id}", accountId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.OK.value()
            response.jsonPath().getString("accountNumber") == "1234"
            response.jsonPath().getString("bankName") == "은행"
            new BigDecimal(response.jsonPath().getString("balance")) == 1_000
            response.jsonPath().getList("trades.id", Long.class).containsAll(1L, 2L, 3L)
        }
    }
}

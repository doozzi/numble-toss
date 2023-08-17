package com.gangbean.stockservice.acceptance

import com.gangbean.stockservice.SpringBootAcceptanceTest
import com.gangbean.stockservice.repository.BankRepository
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
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration

@SpringBootAcceptanceTest
class AccountOpenAcceptanceTest extends Specification {

    @LocalServerPort
    int port

    @Autowired
    BankRepository bankRepository

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
        this.restDocumentation.beforeTest(getClass(), "openAccount_Ok");
    }

    /***
     * given 은행이름과 은행번호, 금액이 주어질 때
     * and 은행이름과 번호에 해당하는 은행이 저장소에 존재하고
     * and 금액이 0원 미만이면
     * when 계좌등록 요청시
     * then 401 Bad Request 응답이 반환된다.
     */
    def openAccount_NotEnoughBalance() {
        given:
        def bankName = "은행"
        def bankNumber = "1"
        def balance = -1_000L
        def param = Map.of(
                "bankName", bankName,
                "bankNumber", bankNumber,
                "balance", balance)

        and:
        bankRepository.findByNameAndNumber(bankName, Long.parseLong(bankNumber)).isPresent()

        and:
        balance < 0

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(param)
                .header("Authorization", token)
                .when()
                .post("/api/accounts")

        then:
        verifyAll {
            response.statusCode() == HttpStatus.BAD_REQUEST.value()
            response.body().jsonPath().getString("message") == "0원 미만의 금액은 입금할 수 없습니다: " + balance
        }
    }

    /**
     * given 은행이름과 은행번호, 금액이 주어질때
     * and 은행 이름과 은행번호에 해당하는 은행이 저장소에 존재하지 않으면
     * when 계좌등록 요청시
     * then 401 Bad Request 응답이 반환된다.
     */
    def openAccount_NotValidBank() {
        given:
        def bankName = "은행"
        def bankNumber = "2"
        def balance = 1_000L
        def param = Map.of(
                "bankName", bankName,
                "bankNumber", bankNumber,
                "balance", balance)

        and:
        bankRepository.findByNameAndNumber(bankName, Long.parseLong(bankNumber)).isEmpty()

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(param)
                .when()
                .post("/api/accounts").then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.BAD_REQUEST.value()
            response.jsonPath().getString("message") == "은행 이름과 번호에 해당하는 은행이 존재하지 않습니다: " + bankName + " / " + bankNumber
        }
    }


    /**
     * given 은행이름과 은행번호, 금액이 주어질때
     * and 금액이 0원 이상이고
     * and 은행이름과 번호에 해당하는 은행이 저장소에 존재하면
     * when 게좌등록 요청시
     * then 계좌의 id, 은행명, 계좌번호, 잔액과 201 Created 응답이 반환된다.
     */
    def openAccount_Ok() {
        given:
        def bankName = "은행"
        def bankNumber = "1"
        def balance = 1_500L
        def param = Map.of(
                "bankName", bankName,
                "bankNumber", bankNumber,
                "balance", balance)

        and:
        bankRepository.findByNameAndNumber(bankName, Long.parseLong(bankNumber)).isPresent()

        and:
        balance >= 0

        when:
        def response = RestAssured.given(this.spec).log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .filter(document("open-account",
                        preprocessRequest(modifyUris()
                                .scheme("https")
                                .host("ec2-43-201-193-154.ap-northeast-2.compute.amazonaws.com")
                                .removePort(),
                                prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("balance").description("초기입금액"),
                                fieldWithPath("bankNumber").description("은행번호"),
                                fieldWithPath("bankName").description("은행명")
                        ),
                        responseFields(
                                fieldWithPath("id").description("계좌ID"),
                                fieldWithPath("accountNumber").description("계좌번호"),
                                fieldWithPath("balance").description("계좌잔액"),
                                fieldWithPath("bankNumber").description("은행번호"),
                                fieldWithPath("bankName").description("은행명")
                        )
                ))
                .body(param)
                .when()
                .post("/api/accounts")

        then:
        verifyAll {
            response.statusCode() == HttpStatus.CREATED.value()
            response.body().jsonPath().getString("id") != null
            response.body().jsonPath().getString("bankName") == bankName
            response.body().jsonPath().getString("accountNumber") != null
            response.body().jsonPath().getLong("balance") == balance
        }
    }
}

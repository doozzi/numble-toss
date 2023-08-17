package com.gangbean.stockservice.acceptance

import com.gangbean.stockservice.SpringBootAcceptanceTest
import com.gangbean.stockservice.jwt.TokenProvider
import com.gangbean.stockservice.repository.AccountRepository
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.specification.RequestSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration

@SpringBootAcceptanceTest
class AccountListAcceptanceTest extends Specification {

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
        this.restDocumentation.beforeTest(getClass(), "계좌목록조회_정상");

    }

    /**
     * given 로그인 한 상태로
     * when 게좌 목록 조회 요청하면
     * then 로그인한 사용자의 계좌 목록이 조회됩니다.
     */
    def "계좌목록조회_정상"() {
        when:
        def response = RestAssured.given(this.spec).log().all()
                .header("Authorization", token)
                .filter(document("get-accounts",
                        preprocessRequest(modifyUris()
                                .scheme("https")
                                .host("ec2-43-201-193-154.ap-northeast-2.compute.amazonaws.com")
                                .removePort(),
                                prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("accounts[]").description("계좌목록"),
                                fieldWithPath("accounts[].id").description("계좌ID"),
                                fieldWithPath("accounts[].accountNumber").description("계좌번호"),
                                fieldWithPath("accounts[].bankName").description("은형맹"),
                                fieldWithPath("accounts[].bankNumber").description("은행번호"),
                                fieldWithPath("accounts[].balance").description("계좌잔액")
                        )
                ))
                .when()
                .get("/api/accounts")
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.OK.value()
            response.jsonPath().getList("accounts.accountNumber", String.class).contains("1234")
            !response.jsonPath().getList("accounts.accountNumber", String.class).contains("1235")
        }
    }
}

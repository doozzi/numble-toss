package com.gangbean.stockservice.acceptance

import com.gangbean.stockservice.SpringBootAcceptanceTest
import com.gangbean.stockservice.jwt.TokenProvider
import com.gangbean.stockservice.repository.AccountRepository
import com.gangbean.stockservice.repository.AccountStockRepository
import com.gangbean.stockservice.repository.StockRepository
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
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration

@SpringBootAcceptanceTest
class StockBuyingAcceptanceTest extends Specification {
    @LocalServerPort
    int port

    @Autowired
    AccountRepository accountRepository

    @Autowired
    StockRepository stockRepository

    @Autowired
    TokenProvider tokenProvider

    @Autowired
    AccountStockRepository accountStockRepository

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
        this.restDocumentation.beforeTest(getClass(), "주식구매요청_정상");

    }

    /**
     * given 로그인한 상태로 계좌ID, 주식ID, 구매량, 가격을 입력하고
     * and 해당 계좌가 존재하고
     * and 해당 게좌가 로그인한 사용자의 계좌이고
     * and 주식이 존재하고
     * and 주식의 잔여량이 구매량보다 많고
     * and 구매가격이 주식의 현재 가격이상이고
     * and 계좌잔액이 충분하면
     * when 주식구매요청시
     * then 201 Created 응답이 기존에 구매한 주식의 ID, 잔여량, 평균금액과 함께 반환되고
     * then 주식의 잔여량이 감소하고
     * then 계좌주식의 잔여량이 늘어납니다.
     */
    def "주식구매요청_정상"(Long accountId, Long stockId, BigDecimal accountStockBalance
                    , BigDecimal averagePrice, BigDecimal totalPaid
                    , BigDecimal accountBalance, BigDecimal stockBalance) {
        given:
        def amount = 5L
        def price = 100L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        def stock = stockRepository.findById(stockId)
        assert stock.isPresent()

        and:
        assert stock.get().howMany() >= amount

        and:
        assert price >= stock.get().howMuch()

        and:
        assert account.get().balance() >= price * amount

        when:
        def response = RestAssured.given(this.spec).log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .filter(document("stock-buying",
                        preprocessRequest(modifyUris()
                                .scheme("https")
                                .host("ec2-43-201-193-154.ap-northeast-2.compute.amazonaws.com")
                                .removePort(),
                                prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("accountId").description("계좌ID"),
                                parameterWithName("stockId").description("주식ID")
                        ),
                        requestFields(
                                fieldWithPath("amount").description("구매요청수량"),
                                fieldWithPath("price").description("구매요청금액")
                        ),
                        responseFields(
                                fieldWithPath("stockId").description("주식ID"),
                                fieldWithPath("amount").description("주식보유량"),
                                fieldWithPath("averagePrice").description("보유주식평균금액")
                        )
                ))
                .body(Map.of("amount", amount, "price", price))
                .when()
                .post("/api/accounts/{accountId}/stocks/{stockId}", accountId, stockId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.CREATED.value()
            response.jsonPath().getLong("stockId") == stockId
            response.jsonPath().getString("amount") as BigDecimal == accountStockBalance
            response.jsonPath().getString("averagePrice") as BigDecimal == averagePrice
            accountRepository.findById(accountId).get().balance() == accountBalance
            stockRepository.findById(stockId).get().howMany() == stockBalance
        }

        where:
        accountId | stockId | accountStockBalance | averagePrice | totalPaid  | accountBalance | stockBalance
        1L        | 1L      | 10_005              | 999          | 10_000_500 | 500            | 95
        1L        | 2L      | 5                   | 100          | 500        | 500            | 95
    }

    /**
     * given 로그인한 상태로 계좌ID, 주식ID, 구매량, 가격을 입력하고
     * and 해당 계좌가 존재하고
     * and 해당 게좌가 로그인한 사용자의 계좌이고
     * and 주식이 존재하고
     * and 주식의 잔여량이 구매량보다 많고
     * and 구매가격이 주식의 현재 가격이상이고
     * and 계좌잔액이 부족하면
     * when 주식구매요청시
     * then 406 Not Acceptable 응답이 반환됩니다.
     */
    def "계좌구매요청_계좌잔액부족"() {
        given:
        def accountId = 1L
        def stockId = 1L
        def amount = 30L
        def price = 100L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        def stock = stockRepository.findById(stockId)
        assert stock.isPresent()

        and:
        assert stock.get().howMany() >= amount

        and:
        assert price >= stock.get().howMuch()

        and:
        assert account.get().balance() < price * amount

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("amount", amount, "price", price))
                .when()
                .post("/api/accounts/{accountId}/stocks/{stockId}", accountId, stockId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.NOT_ACCEPTABLE.value()
            response.jsonPath().getString("message") == "계좌 잔액이 부족합니다: " + account.get().balance()
        }
    }

    /**
     * given 로그인한 상태로 계좌ID, 주식ID, 구매량, 가격을 입력하고
     * and 해당 계좌가 존재하고
     * and 해당 게좌가 로그인한 사용자의 계좌이고
     * and 주식이 존재하고
     * and 주식의 잔여량이 구매량보다 많고
     * and 구매가격이 주식의 현재 가격미만이면
     * when 주식구매요청시
     * then 406 Not Acceptable 응답이 반환됩니다.
     */
    def "계좌구매요청_주식가격미만"() {
        given:
        def accountId = 1L
        def stockId = 1L
        def amount = 10L
        def price = 1L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        def stock = stockRepository.findById(stockId)
        assert stock.isPresent()

        and:
        assert stock.get().howMany() >= amount

        and:
        assert price < stock.get().howMuch()

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("amount", amount, "price", price))
                .when()
                .post("/api/accounts/{accountId}/stocks/{stockId}", accountId, stockId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.NOT_ACCEPTABLE.value()
            response.jsonPath().getString("message") == "주식의 현재가격보다 낮은 가격으로 구매할 수 없습니다: " + stock.get().howMuch()
        }
    }

    /**
     * given 로그인한 상태로 계좌ID, 주식ID, 구매량, 가격을 입력하고
     * and 해당 계좌가 존재하고
     * and 해당 게좌가 로그인한 사용자의 계좌이고
     * and 주식이 존재하고
     * and 주식의 잔여량이 구매량보다 적으면
     * when 주식구매요청시
     * then 406 Not Acceptable 응답이 반환됩니다.
     */
    def "계좌구매요청_주식잔량부족"() {
        given:
        def accountId = 1L
        def stockId = 1L
        def amount = 1_000_000L
        def price = 1_000L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        def stock = stockRepository.findById(stockId)
        assert stock.isPresent()

        and:
        assert stock.get().howMany() < amount

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("amount", amount, "price", price))
                .when()
                .post("/api/accounts/{accountId}/stocks/{stockId}", accountId, stockId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.NOT_ACCEPTABLE.value()
            response.jsonPath().getString("message") == "주식의 잔량이 부족합니다: " + stock.get().howMany()
        }
    }

    /**
     * given 로그인한 상태로 계좌ID, 주식ID, 구매량, 가격을 입력하고
     * and 해당 계좌가 존재하고
     * and 해당 게좌가 로그인한 사용자의 계좌이고
     * and 주식이 존재하지 않으면
     * when 주식구매요청시
     * then 404 Not Found 응답이 반환됩니다.
     */
    def "계좌구매요청_미존재주식"() {
        given:
        def accountId = 1L
        def stockId = 1000L
        def amount = 10L
        def price = 1_000L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        def stock = stockRepository.findById(stockId)
        assert stock.isEmpty()

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("amount", amount, "price", price))
                .when()
                .post("/api/accounts/{accountId}/stocks/{stockId}", accountId, stockId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.NOT_FOUND.value()
            response.jsonPath().getString("message") == "입력된 ID에 해당하는 주식이 존재하지 않습니다: " + stockId
        }
    }

    /**
     * given 로그인한 상태로 계좌ID, 주식ID, 구매량, 가격을 입력하고
     * and 해당 계좌가 존재하고
     * and 해당 게좌가 로그인한 사용자의 계좌가 아닐때
     * when 주식구매요청시
     * then 403 Forbidden 응답이 반환됩니다.
     */
    def "계좌구매요청_타인게좌"() {
        given:
        def accountId = 2L
        def stockId = 1L
        def amount = 10L
        def price = 1_000L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() != username

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("amount", amount, "price", price))
                .when()
                .post("/api/accounts/{accountId}/stocks/{stockId}", accountId, stockId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.FORBIDDEN.value()
            response.jsonPath().getString("message") == "본인의 계좌가 아닙니다: " + accountId
        }
    }

    /**
     * given 로그인한 상태로 계좌ID, 주식ID, 구매량, 가격을 입력하고
     * and 해당 계좌가 존재하지 않으면
     * when 주식구매요청시
     * then 404 Not Found 응답이 반환됩니다.
     */
    def "계좌구매요청_미존재계좌"() {
        given:
        def accountId = 10L
        def stockId = 1L
        def amount = 10L
        def price = 1_000L

        and:
        def account = accountRepository.findById(accountId)
        assert account.isEmpty()

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("amount", amount, "price", price))
                .when()
                .post("/api/accounts/{accountId}/stocks/{stockId}", accountId, stockId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.NOT_FOUND.value()
            response.jsonPath().getString("message") == "입력된 ID에 해당하는 계좌가 존재하지 않습니다: " + accountId
        }
    }
}

package com.gangbean.stockservice.acceptance

import com.gangbean.stockservice.SpringBootAcceptanceTest
import com.gangbean.stockservice.repository.AccountRepository
import com.gangbean.stockservice.repository.AccountStockRepository
import com.gangbean.stockservice.repository.StockRepository
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.specification.RequestSpecification
import org.hibernate.SessionFactory
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
class StockSellingAcceptanceTest extends Specification {
    @LocalServerPort
    int port

    @Autowired
    AccountRepository accountRepository

    @Autowired
    StockRepository stockRepository

    @Autowired
    AccountStockRepository accountStockRepository

    @Autowired
    SessionFactory sessionFactory

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
        this.restDocumentation.beforeTest(getClass(), "계좌판매요청_정상");

    }

    /**
     * given 로그인한 상태로 계좌ID, 주식ID, 판매량, 가격을 입력하고
     * and 해당 계좌가 존재하고
     * and 해당 게좌가 로그인한 사용자의 계좌이고
     * and 주식이 존재하고
     * and 해당 주식을 보유하고 있고
     * and 보유주식의 잔여량이 판매량 이상이고
     * and 판매가격이 주식의 현재가격 이하이면
     * when 주식판매요청시
     * then 201 Created 응답이 판매한 주식의 ID, 잔여량, 평균금액과 함께 반환되고
     * then 주식의 잔여량이 증가하고
     * then 계좌주식의 잔여량이 감소합니다.
     */
    def "계좌판매요청_정상"() {
        given:
        def accountId = 1L
        def stockId = 1L
        def amount = 5L
        def price = 100L

        and:
        def account = accountRepository.findOneWithMemberAndStocksById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        def stock = stockRepository.findById(stockId)
        assert stock.isPresent()

        and:
        def accountStock = account.get().myStock(stockId)
        assert accountStock.isPresent()

        and:
        assert accountStock.get().howMany() >= amount

        and:
        assert price <= stock.get().howMuch()

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
                                parameterWithName("accountId").description("계좌ID"),
                                parameterWithName("stockId").description("주식ID")
                        ),
                        requestFields(
                                fieldWithPath("amount").description("판매요청수량"),
                                fieldWithPath("price").description("판매요청금액")
                        ),
                        responseFields(
                                fieldWithPath("stockId").description("주식ID"),
                                fieldWithPath("amount").description("잔여주식수량"),
                                fieldWithPath("averagePrice").description("잔여주식평균금액")
                        )
                ))
                .body(Map.of("amount", amount, "price", price))
                .when()
                .post("/api/accounts/{accountId}/stocks/{stockId}/selling", accountId, stockId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.CREATED.value()
            response.jsonPath().getLong("stockId") == stockId
            response.jsonPath().getString("amount") as BigDecimal == 9_995
            response.jsonPath().getString("averagePrice") as BigDecimal == 1_000
            accountRepository.findById(accountId).get().balance() == 1500
            stockRepository.findById(stockId).get().howMany() == 105
        }
    }

    /**
     * given 로그인한 상태로 계좌ID, 주식ID, 판매량, 가격을 입력하고
     * and 해당 계좌가 존재하고
     * and 해당 게좌가 로그인한 사용자의 계좌이고
     * and 주식이 존재하고
     * and 해당 주식을 보유하고 있고
     * and 계좌에 보유한 주식이 판매량보다 많고
     * and 판매가격이 주식의 현재 가격을 초과하면
     * when 주식판매요청시
     * then 400 Bad Request 응답이 반환됩니다.
     */
    def "계좌판매요청_주식가격초과"() {
        given:
        def accountId = 1L
        def stockId = 1L
        def amount = 10
        def price = 1_000_000

        and:
        def account = accountRepository.findOneWithMemberAndStocksById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        def stock = stockRepository.findById(stockId)
        assert stock.isPresent()

        and:
        def accountStock = account.get().myStock(stockId)
        assert accountStock.isPresent()

        and:
        assert accountStock.get().howMany() >= amount

        and:
        assert price > stock.get().howMuch()

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("amount", amount, "price", price))
                .when()
                .post("/api/accounts/{accountId}/stocks/{stockId}/selling", accountId, stockId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.BAD_REQUEST.value()
            response.jsonPath().getString("message") == "주식의 현재가격보다 높은 가격으로 판매할 수 없습니다: " + stock.get().howMuch()
        }
    }

    /**
     * given 로그인한 상태로 계좌ID, 주식ID, 판매량, 가격을 입력하고
     * and 해당 계좌가 존재하고
     * and 해당 게좌가 로그인한 사용자의 계좌이고
     * and 주식이 존재하고
     * and 해당 주식을 보유하고 있고
     * and 보유주식의 잔여량이 판매량보다 적으면
     * when 주식판매요청시
     * then 400 Bad Request 응답이 반환됩니다.
     */
    def "계좌판매요청_보유주식잔량부족"() {
        given:
        def accountId = 1L
        def stockId = 1L
        def amount = 1_000_000L
        def price = 1_000L

        and:
        def account = accountRepository.findOneWithMemberAndStocksById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        def stock = stockRepository.findById(stockId)
        assert stock.isPresent()

        and:
        def accountStock = account.get().myStock(stockId)
        assert accountStock.isPresent()

        and:
        assert accountStock.get().howMany() < amount

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("amount", amount, "price", price))
                .when()
                .post("/api/accounts/{accountId}/stocks/{stockId}/selling", accountId, stockId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.BAD_REQUEST.value()
            response.jsonPath().getString("message") == "보유수량이 부족합니다: " + accountStock.get().howMany()
        }
    }

    /**
     * given 로그인한 상태로 계좌ID, 주식ID, 판매량, 가격을 입력하고
     * and 해당 계좌가 존재하고
     * and 해당 계좌가 로그인한 사용자의 계좌이고
     * and 주식이 존재하고
     * and 해당 주식을 보유하고 있지 않을때
     * when 주식판매요청시
     * then 400 Bad Request 응답이 반환됩니다.
     */
    def "계좌판매요청_보유주식아님"() {
        given:
        def accountId = 1L
        def stockId = 10L
        def amount = 1_000_000L
        def price = 1_000L

        and:
        def account = accountRepository.findOneWithMemberAndStocksById(accountId)
        assert account.isPresent()

        and:
        assert account.get().whose().getUsername() == username

        and:
        def stock = stockRepository.findById(stockId)
        assert stock.isPresent()

        and:
        def accountStock = account.get().myStock(stockId)
        assert accountStock.isEmpty()

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(Map.of("amount", amount, "price", price))
                .when()
                .post("/api/accounts/{accountId}/stocks/{stockId}/selling", accountId, stockId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.NOT_FOUND.value()
            response.jsonPath().getString("message") == "보유한 주식이 아닙니다: " + stockId
        }
    }

    /**
     * given 로그인한 상태로 계좌ID, 주식ID, 판매량, 가격을 입력하고
     * and 해당 계좌가 존재하고
     * and 해당 계좌가 로그인한 사용자의 계좌이고
     * and 주식이 존재하지 않으면
     * when 주식판매요청시
     * then 404 Not Found 응답이 반환됩니다.
     */
    def "계좌판매요청_미존재주식"() {
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
                .post("/api/accounts/{accountId}/stocks/{stockId}/selling", accountId, stockId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.NOT_FOUND.value()
            response.jsonPath().getString("message") == "입력된 ID에 해당하는 주식이 존재하지 않습니다: " + stockId
        }
    }

    /**
     * given 로그인한 상태로 계좌ID, 주식ID, 판매량, 가격을 입력하고
     * and 해당 계좌가 존재하고
     * and 해당 계좌가 로그인한 사용자의 계좌가 아닐때
     * when 주식판매요청시
     * then 403 Forbidden 응답이 반환됩니다.
     */
    def "계좌판매요청_타인계좌"() {
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
                .post("/api/accounts/{accountId}/stocks/{stockId}/selling", accountId, stockId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.FORBIDDEN.value()
            response.jsonPath().getString("message") == "본인의 계좌가 아닙니다: " + accountId
        }
    }

    /**
     * given 로그인한 상태로 계좌ID, 주식ID, 판매량, 가격을 입력하고
     * and 해당 계좌가 존재하지 않으면
     * when 주식판매요청시
     * then 404 Not Found 응답이 반환됩니다.
     */
    def "계좌판매요청_미존재계좌"() {
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
                .post("/api/accounts/{accountId}/stocks/{stockId}/selling", accountId, stockId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.NOT_FOUND.value()
            response.jsonPath().getString("message") == "입력된 ID에 해당하는 계좌가 존재하지 않습니다: " + accountId
        }
    }
}

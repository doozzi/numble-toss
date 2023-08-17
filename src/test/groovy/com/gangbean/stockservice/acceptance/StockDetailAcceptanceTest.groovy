package com.gangbean.stockservice.acceptance

import com.gangbean.stockservice.SpringBootAcceptanceTest
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

import java.util.stream.Collectors

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
class StockDetailAcceptanceTest extends Specification{

    @LocalServerPort
    int port

    @Autowired
    StockRepository stockRepository

    String token

    public ManualRestDocumentation restDocumentation = new ManualRestDocumentation()

    private RequestSpecification spec

    def setup() {
        RestAssured.port = port
        def loginResponse = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("username", "admin", "password", "admin"))
                .when()
                .post("/api/login")
                .then().log().all()
                .extract()
        token = loginResponse.header("Authorization")
        this.spec = new RequestSpecBuilder().addFilter(
                documentationConfiguration(this.restDocumentation))
                .build()
        this.restDocumentation.beforeTest(getClass(), "주식상세조회_정상");

    }

    /**
     * given 주식 ID를 입력하고
     * and 해당하는 주식이 존재하면
     * when 주식상세정보 조회요청시
     * then 200 Ok와 주식의 과거 가격정보를 포함한 주식 상세정보가 반환됩니다.
     */
    def "주식상세조회_정상"() {
        given:
        def stockId = 1L

        and:
        assert stockRepository.findById(stockId)
                .isPresent()

        when:
        def response = RestAssured.given(this.spec).log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .filter(document("get-stock-detail",
                        preprocessRequest(modifyUris()
                                .scheme("https")
                                .host("ec2-43-201-193-154.ap-northeast-2.compute.amazonaws.com")
                                .removePort(),
                                prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("주식ID")
                        ),
                        responseFields(
                                fieldWithPath("stockId").description("주식ID"),
                                fieldWithPath("stockName").description("주식명"),
                                fieldWithPath("histories[]").description("주식가격이력"),
                                fieldWithPath("histories[].price").description("과거가격"),
                                fieldWithPath("histories[].createdAt").description("기준일시"),
                        )
                ))
                .when()
                .get("/api/stocks/{id}", stockId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.OK.value()
            response.jsonPath().getLong("stockId") == stockId
            response.jsonPath().getString("stockName") == "백만전자"
            response.jsonPath().getList("histories.price").stream()
                    .map(BigDecimal::new)
                    .collect(Collectors.toList()).containsAll(new BigDecimal(80), new BigDecimal(90))
        }
    }

    /**
     * given 주식 ID를 입력하고
     * and 해당하는 주식이 존재하지 않을때
     * when 주식상세정보 조회요청을 하면
     * then 404 Not Found 응답이 반환됩니다.
     */
    def "주식상세조회_미존재주식"() {
        given:
        def stockId = 1010L

        and:
        assert stockRepository.findById(stockId)
                .isEmpty()

        when:
        def response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .when()
                .get("/api/stocks/{id}", stockId)
                .then().log().all()
                .extract()

        then:
        verifyAll {
            response.statusCode() == HttpStatus.NOT_FOUND.value()
            response.jsonPath().getString("message") == "입력된 ID에 해당하는 주식이 존재하지 않습니다: " + stockId
        }
    }

}

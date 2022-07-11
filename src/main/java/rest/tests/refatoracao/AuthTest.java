package rest.tests.refatoracao;

import static io.restassured.RestAssured.given;
import org.junit.Test;
import io.restassured.RestAssured;
import io.restassured.specification.FilterableRequestSpecification;
import rest.core.BaseTest;


public class AuthTest extends BaseTest {
	
	@Test
	public void naoDeveAcessarAPISemToken() {
		FilterableRequestSpecification req = (FilterableRequestSpecification) RestAssured.requestSpecification;
		req.removeHeader("Authorization");
		
		given()
			.when()
				.get("/contas")
			.then()
				.statusCode(401)
		;
	}
	
}
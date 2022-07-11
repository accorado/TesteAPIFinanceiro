package rest.tests.refatoracao.suite;

import io.restassured.RestAssured;
import org.junit.runner.RunWith;
import rest.core.BaseTest;
import rest.tests.refatoracao.AuthTest;
import rest.tests.refatoracao.ContasTest;
import rest.tests.refatoracao.MovimentacoesTest;
import rest.tests.refatoracao.SaldoTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;

import static io.restassured.RestAssured.given;

@RunWith(org.junit.runners.Suite.class)
@org.junit.runners.Suite.SuiteClasses({
		ContasTest.class,
		MovimentacoesTest.class,
		SaldoTest.class,
		AuthTest.class
		
})
public class Suite extends BaseTest {
	@BeforeClass
	public static void login() {
		Map<String, String> login = new HashMap<>();
		login.put("email", "anna@teste.com");
		login.put("senha", "123456");
		String TOKEN = given()
				.body(login)
			.when()
				.post("/signin")
			.then()
				.statusCode(200)
				.extract().path("token");
		
		RestAssured.requestSpecification.header("Authorization", "JWT " + TOKEN);
		
		RestAssured.get("/reset").then().statusCode(200);
	}
}


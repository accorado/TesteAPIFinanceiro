package rest.tests.refatoracao;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import org.junit.Test;
import rest.core.BaseTest;
import rest.core.utils.BarrigaUtils;


public class SaldoTest extends BaseTest {
	
	@Test
	public void deveCalcularSaldoContas() {
		Integer CONTA_ID = BarrigaUtils.getIdContaPeloNome("Conta para saldo");
		
		given()
			.when()
				.get("/saldo")
			.then()
				.statusCode(200)
				.body("find{it.conta_id == "+CONTA_ID+"}.saldo", is("534.00"))
		;
	}
}
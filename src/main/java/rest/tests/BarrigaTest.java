package rest.tests;

import io.restassured.RestAssured;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import rest.core.BaseTest;
import java.util.HashMap;
import java.util.Map;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import io.restassured.specification.FilterableRequestSpecification;
import rest.core.utils.DataUtils;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class BarrigaTest extends BaseTest {
	
	private static String CONTA_NAME = "Conta " + System.nanoTime();
	private static Integer CONTA_ID;
	private static Integer MOV_ID;
	
	
	@BeforeClass
	public void login() {
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
	}
	@Test
	public void t02_deveIncluirContaComSucesso() {
		CONTA_ID = given()
				.body("{ \"nome\": \""+CONTA_NAME+"\" }")
			.when()
				.post("/contas")
			.then()
				.statusCode(201)
				.extract().path("id")
		;
	}
	
	@Test
	public void t03_deveAlterarContaComSucesso() {
		
		
		given()
				.body("{ \"nome\": \""+CONTA_NAME+" teste de api - v2\" }")
			.when()
				.put("/contas/{id}")
			.then()
				.statusCode(200)
				.body("nome", is(CONTA_NAME+"teste de api - v2"))
		;
	}
	
	@Test
	public void t04_naoDeveInserirContaComMesmoNome() {
		given()
				.body("{ \"nome\": \""+CONTA_NAME+" teste de api - v2\" }")
			.when()
				.post("/contas")
			.then()
				.statusCode(400)
				.body("error", is("Já existe uma conta com esse nome!"))
		
		;
	}
	
	@Test
	public void t05_deveInserirMovimentacao() {
		Movimentacao mov = getMovimentacaoValida();
	
		MOV_ID = given()
				.body(mov)
			.when()
				.post("/transacoes")
			.then()
				.statusCode(201)
				.extract().path("id")
		;
	}
	
	@Test
	public void t06_deveValidarCamposObrigatoriosMovimentacao() {
		
		given()
				.body("{}");
			when()
				.post("/transacoes")
			.then()
				.statusCode(400)
				.body("$", hasSize(8))
				.body("msg", hasItems(
						"Data da Movimentação é obrigatório",
						"Data do pagamento é obrigatório",
						"Descrição é obrigatório",
						"Interessado é obrigatório",
						"Valor é obrigatório",
						"Valor deve ser um número",
						"Conta é obrigatório",
						"Situação é obrigatório"
				))
		;
	}
	
	@Test
	public void t07_naoCadastrarMovimentacaoFutura() {
		Movimentacao mov = getMovimentacaoValida();
		mov.setData_transacao(DataUtils.getDataDiferencaDias(2));
		
		given()
				.body(mov)
			.when()
				.post("/transacoes")
			.then()
				.statusCode(400)
				.body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"))
		;
	}
	
	@Test
	public void t08_naoDeveRemoverContaComMovimentacao(){
		given()
				.pathParam("id", CONTA_ID)
			.when()
				.delete("/contas/{id}")
			.then()
				.statusCode(500)
				.body("constraint", is("transacoes_conta_id_foreign"))
		;
	}

	@Test
	public void t09_deveCalcularSaldoContas(){
		given()
			.when()
				.get("/saldo")
			.then()
				.statusCode(200)
				.body("find{it.conta_id == "+CONTA_ID+"}.saldo", is("100.00"))
		;
	}
	
	@Test
	public void t10_deveRemoverMovimentacao(){
		given()
				.pathParam("id", MOV_ID)
			.when()
				.delete("/transacoes/{id}")
			.then()
				.statusCode(204)
		;
	}
	
	@Test
	public void t11_naoDeveAcessarAPISemToken() {
		FilterableRequestSpecification req = (FilterableRequestSpecification) RestAssured.requestSpecification;
		req.removeHeader("Authorization");
		
		given()
			.when()
				.get("/contas")
			.then()
				.statusCode(401)
		;
	}
	
	private Movimentacao getMovimentacaoValida() {
		Movimentacao mov = new Movimentacao();
		mov.setConta_id(1254540);
		//mov.setUsuario_id();
		mov.setDescricao("Descricao da movimentacao");
		mov.setEnvolvido("Envolvido na movimentacao");
		mov.setTipo("REC");
		mov.setData_transacao("08/07/2022");
		mov.setData_pagamento("10/07/2022");
		mov.setValor(200f);
		mov.setStatus(true);
		return mov;
	}
	
}

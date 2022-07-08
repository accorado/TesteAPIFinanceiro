package rest.tests;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import rest.core.BaseTest;
import java.util.HashMap;
import java.util.Map;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class BarrigaTest extends BaseTest {
	
	private String TOKEN;
	
	@Before
	public void login() {
		Map<String, String> login = new HashMap<>();
		login.put("email", "anna@teste.com");
		login.put("senha", "123456");
		
		TOKEN = given()
				.body(login)
			.when()
				.post("/signin")
			.then()
				.statusCode(200)
				.extract().path("token");
	}
	
	@Test
	public void naoDeveAcessarAPISemToken() {
		given()
			.when()
				.get("/contas")
			.then()
				.statusCode(401)
		;
	}
	
	@Test
	public void deveIncluirContaComSucesso() {
		
		given()
				.header("Authorization", "JWT " + TOKEN)
				.body("{ \"nome\": \"teste de api\" }")
			.when()
				.post("/contas")
			.then()
				.statusCode(201)
		;
	}
	
	@Test
	public void deveAlterarContaComSucesso() {
		
		given()
				.header("Authorization", "JWT " + TOKEN)
				.body("{ \"nome\": \"teste de api - v2\" }")
			.when()
				.put("/contas/1254540")
			.then()
				.statusCode(200)
				.body("nome", is("teste de api - v2"))
		;
	}
	
	@Test
	public void naoDeveInserirContaComMesmoNome() {
		given()
				.header("Authorization", "JWT " + TOKEN)
				.body("{ \"nome\": \"teste de api - v2\" }")
			.when()
				.post("/contas")
			.then()
				.statusCode(400)
				.body("error", is("Já existe uma conta com esse nome!"))
		
		;
	}
	
	@Test
	public void deveInserirMovimentacao() {
		Movimentacao mov = getMovimentacaoValida();
	
		
		given()
				.header("Authorization", "JWT " + TOKEN)
				.body(mov)
			.when()
				.post("/transacoes")
			.then()
				.statusCode(201)
		;
	}
	
	@Test
	public void deveValidarCamposObrigatoriosMovimentacao() {
		
		given()
				.header("Authorization", "JWT " + TOKEN)
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
	public void naoCadastrarMovimentacaoFutura() {
		Movimentacao mov = getMovimentacaoValida();
		mov.setData_transacao("15/07/2022");
		
		given()
				.header("Authorization", "JWT " + TOKEN)
				.body(mov)
			.when()
				.post("/transacoes")
			.then()
				.statusCode(400)
				.body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"))
		;
	}
	
	@Test
	public void naoDeveRemoverContaComMovimentacao(){
		given()
				.header("Authorization", "JWT " + TOKEN)
			.when()
				.delete("/contas/1254540")
			.then()
				.statusCode(500)
				.body("constraint", is("transacoes_conta_id_foreign"))
		;
	}

	@Test
	public void deveCalcularSaldoContas(){
		given()
				.header("Authorization", "JWT " + TOKEN)
			.when()
				.get("/saldo")
			.then()
				.statusCode(200)
				.body("find{it.conta_id == 1254540}.saldo", is("200.00"))
		;
	}
	
	@Test
	public void deveRemoverMovimentacao(){
		//"/removerMovimentacao?id=1178192"
		given()
				.header("Authorization", "JWT " + TOKEN)
			.when()
				.delete("/transacoes/1178684")
			.then()
				.statusCode(204)
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

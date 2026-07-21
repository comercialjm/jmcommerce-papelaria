package com.jmcodestudio.papelaria;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test do M3: garante que o schema (Flyway), os dados de exemplo (V2) e a
 * API de produtos/categorias funcionam ponta a ponta contra um Postgres real.
 * Exige um banco disponível — o mesmo usado pelo docker-compose local ou pelo
 * serviço Postgres do CI.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class ProdutoCategoriaApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void deveListarCategoriasDeNivel1Ativas() {
        ResponseEntity<String> resposta = restTemplate.getForEntity(
                url("/api/categorias"), String.class);

        assertThat(resposta.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resposta.getBody()).contains("Cadernos", "Agendas", "Canetas");
        // "Espiral" é subcategoria (RN-38) e não deve aparecer na navegação pública
        assertThat(resposta.getBody()).doesNotContain("Espiral");
    }

    @Test
    void deveListarProdutosDoCatalogo() {
        ResponseEntity<String> resposta = restTemplate.getForEntity(
                url("/api/produtos"), String.class);

        assertThat(resposta.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resposta.getBody()).contains("Caderno floral A5");
    }

    @Test
    void produtoInexistenteDeveRetornar404() {
        ResponseEntity<String> resposta = restTemplate.getForEntity(
                url("/api/produtos/999999"), String.class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}

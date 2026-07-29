package com.jmcodestudio.papelaria;

import com.jmcodestudio.papelaria.entity.Categoria;
import com.jmcodestudio.papelaria.entity.Produto;
import com.jmcodestudio.papelaria.repository.CategoriaRepository;
import com.jmcodestudio.papelaria.repository.ProdutoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test do M3: garante que o schema (Flyway) e a API de produtos/categorias
 * funcionam ponta a ponta contra um Postgres real.
 *
 * Cria seus PRÓPRIOS dados de teste (em vez de depender dos dados de exemplo da
 * V2) — a V6 remove esses dados de exemplo antes do lançamento (M10), então este
 * teste não pode mais presumir que eles existem. @Transactional não é usado aqui
 * de propósito: como o teste sobe um servidor real (RANDOM_PORT) e faz chamadas
 * HTTP de verdade, a thread que atende a requisição usa uma conexão separada da
 * transação do teste, e não enxergaria dados ainda não commitados.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class ProdutoCategoriaApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    private Long produtoId;
    private Long subcategoriaId;
    private Long categoriaId;

    @BeforeEach
    void criarDadosDeTeste() {
        Categoria categoria = new Categoria();
        categoria.setNome("Cadernos Integração Teste");
        categoria.setAtiva(true);
        categoria = categoriaRepository.save(categoria);
        categoriaId = categoria.getId();

        Categoria subcategoria = new Categoria();
        subcategoria.setNome("Espiral Integração Teste");
        subcategoria.setAtiva(true);
        subcategoria.setParent(categoria);
        subcategoria = categoriaRepository.save(subcategoria);
        subcategoriaId = subcategoria.getId();

        Produto produto = new Produto();
        produto.setNome("Caderno Floral Integração Teste");
        produto.setDescricao("Produto criado só para este teste de integração.");
        produto.setPreco(new BigDecimal("39.90"));
        produto.setEstoque(10);
        produto.setAtivo(true);
        produto.setCategoria(categoria);
        produto.setPesoGramas(300);
        produto.setLarguraCm(new BigDecimal("20"));
        produto.setAlturaCm(new BigDecimal("15"));
        produto.setComprimentoCm(new BigDecimal("5"));
        produto = produtoRepository.save(produto);
        produtoId = produto.getId();
    }

    @AfterEach
    void limparDadosDeTeste() {
        produtoRepository.deleteById(produtoId);
        categoriaRepository.deleteById(subcategoriaId);
        categoriaRepository.deleteById(categoriaId);
    }

    @Test
    void deveListarCategoriasDeNivel1Ativas() {
        ResponseEntity<String> resposta = restTemplate.getForEntity(
                url("/api/categorias"), String.class);

        assertThat(resposta.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resposta.getBody()).contains("Cadernos Integração Teste");
        // RN-38: subcategoria não deve aparecer na navegação pública (nível 1 apenas)
        assertThat(resposta.getBody()).doesNotContain("Espiral Integração Teste");
    }

    @Test
    void deveListarProdutosDoCatalogo() {
        ResponseEntity<String> resposta = restTemplate.getForEntity(
                url("/api/produtos"), String.class);

        assertThat(resposta.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resposta.getBody()).contains("Caderno Floral Integração Teste");
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

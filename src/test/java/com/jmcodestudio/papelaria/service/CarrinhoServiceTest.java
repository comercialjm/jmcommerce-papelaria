package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.dto.CarrinhoDTOs.ItemRequisicao;
import com.jmcodestudio.papelaria.dto.CarrinhoDTOs.ItemValidado;
import com.jmcodestudio.papelaria.dto.CarrinhoDTOs.RequisicaoValidacao;
import com.jmcodestudio.papelaria.dto.CarrinhoDTOs.RespostaValidacao;
import com.jmcodestudio.papelaria.entity.Produto;
import com.jmcodestudio.papelaria.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class CarrinhoServiceTest {

    private ProdutoRepository produtoRepository;
    private CarrinhoService carrinhoService;

    @BeforeEach
    void setUp() {
        produtoRepository = Mockito.mock(ProdutoRepository.class);
        carrinhoService = new CarrinhoService(produtoRepository);
    }

    private Produto produtoAtivo(long id, String nome, String preco, int estoque) {
        Produto p = new Produto();
        p.setId(id);
        p.setNome(nome);
        p.setPreco(new BigDecimal(preco));
        p.setEstoque(estoque);
        p.setAtivo(true);
        return p;
    }

    @Test
    void validar_deveMarcarIndisponivel_quandoProdutoNaoExiste() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.empty());

        RespostaValidacao resposta = carrinhoService.validar(
                new RequisicaoValidacao(List.of(new ItemRequisicao(1L, 2))));

        ItemValidado item = resposta.itens().get(0);
        assertThat(item.disponivel()).isFalse();
        assertThat(resposta.subtotal()).isEqualByComparingTo("0");
    }

    @Test
    void validar_deveMarcarIndisponivel_quandoProdutoInativoOuSemEstoque() {
        Produto inativo = produtoAtivo(1L, "Produto X", "10.00", 5);
        inativo.setAtivo(false);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(inativo));

        RespostaValidacao resposta = carrinhoService.validar(
                new RequisicaoValidacao(List.of(new ItemRequisicao(1L, 1))));

        assertThat(resposta.itens().get(0).disponivel()).isFalse();
    }

    @Test
    void validar_deveAjustarQuantidade_quandoMaiorQueEstoqueDisponivel() {
        Produto produto = produtoAtivo(1L, "Caderno", "39.90", 3);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        RespostaValidacao resposta = carrinhoService.validar(
                new RequisicaoValidacao(List.of(new ItemRequisicao(1L, 10))));

        ItemValidado item = resposta.itens().get(0);
        assertThat(item.disponivel()).isTrue();
        assertThat(item.quantidade()).isEqualTo(3);
        assertThat(item.quantidadeAjustada()).isTrue();
        assertThat(resposta.subtotal()).isEqualByComparingTo("119.70"); // 39.90 * 3
    }

    @Test
    void validar_deveUsarPrecoAtualDoBanco_ignorandoQualquerSnapshotDoCliente() {
        Produto produto = produtoAtivo(1L, "Caneta", "5.00", 10);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        RespostaValidacao resposta = carrinhoService.validar(
                new RequisicaoValidacao(List.of(new ItemRequisicao(1L, 2))));

        assertThat(resposta.itens().get(0).preco()).isEqualByComparingTo("5.00");
        assertThat(resposta.itens().get(0).quantidadeAjustada()).isFalse();
    }
}

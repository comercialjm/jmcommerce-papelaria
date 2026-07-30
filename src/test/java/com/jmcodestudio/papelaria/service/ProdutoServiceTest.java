package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.dto.ProdutoDTOs.Formulario;
import com.jmcodestudio.papelaria.entity.Categoria;
import com.jmcodestudio.papelaria.entity.Produto;
import com.jmcodestudio.papelaria.exception.RecursoNaoEncontradoException;
import com.jmcodestudio.papelaria.repository.CategoriaRepository;
import com.jmcodestudio.papelaria.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProdutoServiceTest {

    private ProdutoRepository produtoRepository;
    private CategoriaService categoriaService;
    private CategoriaRepository categoriaRepository;
    private ProdutoService produtoService;

    @BeforeEach
    void setUp() {
        produtoRepository = Mockito.mock(ProdutoRepository.class);
        categoriaService = Mockito.mock(CategoriaService.class);
        categoriaRepository = Mockito.mock(CategoriaRepository.class);
        produtoService = new ProdutoService(produtoRepository, categoriaService, categoriaRepository);
    }

    @Test
    void criar_deveAplicarValoresPadrao_quandoPesoEDimensoesNaoInformados() {
        Categoria categoria = new Categoria();
        categoria.setId(5L);
        categoria.setNome("Cadernos");

        when(categoriaService.buscarEntidadeOuFalhar(5L)).thenReturn(categoria);
        when(produtoRepository.save(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));

        Formulario form = new Formulario(
                "Caderno Teste", "Descrição", new BigDecimal("29.90"), 10, 5L,
                List.of("http://img.com/1.png"),
                null, null, null, null // RN-16: tudo nulo, deve virar padrão
        );

        produtoService.criar(form);

        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);
        Mockito.verify(produtoRepository).save(captor.capture());
        Produto salvo = captor.getValue();

        assertThat(salvo.getPesoGramas()).isEqualTo(300);
        assertThat(salvo.getLarguraCm()).isEqualByComparingTo("20");
        assertThat(salvo.getAlturaCm()).isEqualByComparingTo("15");
        assertThat(salvo.getComprimentoCm()).isEqualByComparingTo("5");
    }

    @Test
    void criar_deveRespeitarValoresInformados_quandoPesoEDimensoesPreenchidos() {
        Categoria categoria = new Categoria();
        categoria.setId(5L);

        when(categoriaService.buscarEntidadeOuFalhar(5L)).thenReturn(categoria);
        when(produtoRepository.save(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));

        Formulario form = new Formulario(
                "Caderno Pesado", "Descrição", new BigDecimal("29.90"), 10, 5L,
                List.of("http://img.com/1.png"),
                800, new BigDecimal("30"), new BigDecimal("25"), new BigDecimal("4")
        );

        produtoService.criar(form);

        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);
        Mockito.verify(produtoRepository).save(captor.capture());
        Produto salvo = captor.getValue();

        assertThat(salvo.getPesoGramas()).isEqualTo(800);
        assertThat(salvo.getLarguraCm()).isEqualByComparingTo("30");
    }

    @Test
    void buscarDetalhePublico_deveLancarExcecao_quandoProdutoInativo() {
        Produto produto = new Produto();
        produto.setId(1L);
        produto.setAtivo(false);
        produto.setCategoria(new Categoria());

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> produtoService.buscarDetalhePublico(1L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void buscarDetalhePublico_deveLancarExcecao_quandoProdutoNaoExiste() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.buscarDetalhePublico(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void listarCatalogo_deveIncluirProdutosDaSubcategoria_aoFiltrarPelaCategoriaPai() {
        Categoria espiral = new Categoria();
        espiral.setId(20L);

        when(categoriaRepository.findByParentId(10L)).thenReturn(List.of(espiral));
        when(produtoRepository.findByAtivoTrueAndCategoriaIdIn(eq(List.of(10L, 20L)), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        produtoService.listarCatalogo(10L, org.springframework.data.domain.PageRequest.of(0, 12));

        verify(produtoRepository).findByAtivoTrueAndCategoriaIdIn(List.of(10L, 20L), org.springframework.data.domain.PageRequest.of(0, 12));
    }
}

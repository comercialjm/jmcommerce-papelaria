package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.dto.CategoriaDTOs.Formulario;
import com.jmcodestudio.papelaria.entity.Categoria;
import com.jmcodestudio.papelaria.exception.RegraDeNegocioException;
import com.jmcodestudio.papelaria.repository.CategoriaRepository;
import com.jmcodestudio.papelaria.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/** Cobre RN-36, RN-37 e RN-39 — as regras de negócio mais importantes da CategoriaService. */
@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void criar_deveLancarExcecao_quandoNomeJaExisteNoMesmoNivel() {
        Categoria existente = new Categoria();
        existente.setId(1L);
        existente.setNome("Cadernos");

        when(categoriaRepository.findConflitoDeNomeNoMesmoNivel(eq("Cadernos"), eq(null), eq(null)))
                .thenReturn(Optional.of(existente));

        Formulario form = new Formulario("Cadernos", null, null);

        assertThatThrownBy(() -> categoriaService.criar(form))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("RN-37");
    }

    @Test
    void excluir_deveLancarExcecao_quandoHaProdutosAtivosVinculados() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.existsByCategoriaIdAndAtivoTrue(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.excluir(1L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("RN-36");
    }

    @Test
    void excluir_deveLancarExcecao_quandoHaSubcategorias() {
        Categoria pai = new Categoria();
        pai.setId(1L);
        Categoria filha = new Categoria();
        filha.setId(2L);
        pai.setSubcategorias(List.of(filha));

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(pai));
        when(produtoRepository.existsByCategoriaIdAndAtivoTrue(1L)).thenReturn(false);

        assertThatThrownBy(() -> categoriaService.excluir(1L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("subcategorias");
    }

    @Test
    void alterarStatus_deveDesativarSubcategoriasEmCascata_aoDesativarPai() {
        Categoria pai = new Categoria();
        pai.setId(1L);
        pai.setAtiva(true);

        Categoria filha = new Categoria();
        filha.setId(2L);
        filha.setAtiva(true);
        filha.setSubcategorias(List.of());

        pai.setSubcategorias(List.of(filha));

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(pai));

        categoriaService.alterarStatus(1L, false);

        assertThat(pai.isAtiva()).isFalse();
        assertThat(filha.isAtiva()).isFalse();
    }

    @Test
    void alterarStatus_naoDeveMexerNasSubcategorias_aoAtivar() {
        Categoria pai = new Categoria();
        pai.setId(1L);
        pai.setAtiva(false);

        Categoria filha = new Categoria();
        filha.setId(2L);
        filha.setAtiva(false);
        pai.setSubcategorias(List.of(filha));

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(pai));

        categoriaService.alterarStatus(1L, true);

        assertThat(pai.isAtiva()).isTrue();
        assertThat(filha.isAtiva()).isFalse(); // RN-39 só fala de cascata ao desativar
    }
}

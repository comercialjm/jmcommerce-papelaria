package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.dto.FreteDTOs.ItemFrete;
import com.jmcodestudio.papelaria.dto.FreteDTOs.Opcao;
import com.jmcodestudio.papelaria.dto.FreteDTOs.Requisicao;
import com.jmcodestudio.papelaria.dto.MelhorEnvioDTOs.Empresa;
import com.jmcodestudio.papelaria.dto.MelhorEnvioDTOs.OpcaoResposta;
import com.jmcodestudio.papelaria.entity.Categoria;
import com.jmcodestudio.papelaria.entity.Produto;
import com.jmcodestudio.papelaria.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class FreteServiceTest {

    private ProdutoRepository produtoRepository;
    private ConfiguracaoLojaService configuracaoLojaService;
    private MelhorEnvioClient melhorEnvioClient;
    private FreteService freteService;

    @BeforeEach
    void setUp() {
        produtoRepository = Mockito.mock(ProdutoRepository.class);
        configuracaoLojaService = Mockito.mock(ConfiguracaoLojaService.class);
        melhorEnvioClient = Mockito.mock(MelhorEnvioClient.class);
        freteService = new FreteService(produtoRepository, configuracaoLojaService, melhorEnvioClient);
    }

    private Produto produtoValido(long id) {
        Produto p = new Produto();
        p.setId(id);
        p.setAtivo(true);
        p.setPreco(new BigDecimal("29.90"));
        p.setPesoGramas(300);
        p.setLarguraCm(new BigDecimal("20"));
        p.setAlturaCm(new BigDecimal("15"));
        p.setComprimentoCm(new BigDecimal("5"));
        p.setCategoria(new Categoria());
        return p;
    }

    @Test
    void calcular_deveRetornarListaVazia_semChamarMelhorEnvio_quandoNenhumProdutoValido() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.empty());

        List<Opcao> resultado = freteService.calcular(
                new Requisicao("01310100", List.of(new ItemFrete(1L, 1))));

        assertThat(resultado).isEmpty();
        Mockito.verifyNoInteractions(melhorEnvioClient);
    }

    @Test
    void calcular_deveFiltrarOpcoesComErro() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoValido(1L)));
        when(configuracaoLojaService.buscarCepOrigem()).thenReturn("28621-350");

        OpcaoResposta comErro = new OpcaoResposta(1L, "PAC", new BigDecimal("20"), null, 5, null,
                new Empresa("Correios"), "Serviço indisponível");
        OpcaoResposta semErro = new OpcaoResposta(2L, "SEDEX", new BigDecimal("35"), null, 2, null,
                new Empresa("Correios"), null);

        when(melhorEnvioClient.calcular(any())).thenReturn(List.of(comErro, semErro));

        List<Opcao> resultado = freteService.calcular(
                new Requisicao("01310100", List.of(new ItemFrete(1L, 1))));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).servico()).isEqualTo("SEDEX");
    }

    @Test
    void calcular_devePreferirPrecoEPrazoCustomizados_quandoDisponiveis() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoValido(1L)));
        when(configuracaoLojaService.buscarCepOrigem()).thenReturn("28621-350");

        OpcaoResposta opcao = new OpcaoResposta(1L, "PAC", new BigDecimal("20"),
                new BigDecimal("15"), 5, 3, new Empresa("Correios"), null);

        when(melhorEnvioClient.calcular(any())).thenReturn(List.of(opcao));

        List<Opcao> resultado = freteService.calcular(
                new Requisicao("01310100", List.of(new ItemFrete(1L, 1))));

        assertThat(resultado.get(0).preco()).isEqualByComparingTo("15");
        assertThat(resultado.get(0).prazoDias()).isEqualTo(3);
    }
}

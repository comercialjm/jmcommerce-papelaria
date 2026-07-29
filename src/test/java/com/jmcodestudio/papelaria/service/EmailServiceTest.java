package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.entity.Pedido;
import com.jmcodestudio.papelaria.entity.PedidoEmailLog;
import com.jmcodestudio.papelaria.entity.PedidoItem;
import com.jmcodestudio.papelaria.entity.TipoEmail;
import com.jmcodestudio.papelaria.repository.PedidoEmailLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** RN-26: garante que o resultado do envio (sucesso/falha) é sempre registrado, nunca lançado como exceção. */
class EmailServiceTest {

    private ResendClient resendClient;
    private PedidoEmailLogRepository emailLogRepository;
    private ConfiguracaoLojaService configuracaoLojaService;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        resendClient = Mockito.mock(ResendClient.class);
        emailLogRepository = Mockito.mock(PedidoEmailLogRepository.class);
        configuracaoLojaService = Mockito.mock(ConfiguracaoLojaService.class);
        emailService = new EmailService(resendClient, emailLogRepository, configuracaoLojaService);

        when(configuracaoLojaService.buscarContatoEmail()).thenReturn("contato@lojadepapelaria.com.br");
    }

    private Pedido pedidoDeTeste() {
        Pedido pedido = new Pedido();
        pedido.setNumero("PED123");
        pedido.setClienteEmail("cliente@teste.com");
        pedido.setEnderecoRua("Rua Teste");
        pedido.setEnderecoNumero("123");
        pedido.setEnderecoBairro("Bairro");
        pedido.setEnderecoCidade("Cidade");
        pedido.setEnderecoUf("RJ");
        pedido.setEnderecoCep("28621350");
        pedido.setTotal(new BigDecimal("100.00"));

        PedidoItem item = new PedidoItem();
        item.setProdutoNome("Caderno");
        item.setQuantidade(2);
        item.setPrecoUnitario(new BigDecimal("50.00"));
        pedido.adicionarItem(item);

        return pedido;
    }

    @Test
    void enviarConfirmacaoPagamento_deveRegistrarLogDeSucesso_quandoEnvioFunciona() {
        Pedido pedido = pedidoDeTeste();
        when(resendClient.enviar(eq("cliente@teste.com"), anyString(), anyString())).thenReturn(true);

        emailService.enviarConfirmacaoPagamento(pedido);

        ArgumentCaptor<PedidoEmailLog> captor = ArgumentCaptor.forClass(PedidoEmailLog.class);
        verify(emailLogRepository).save(captor.capture());

        PedidoEmailLog registro = captor.getValue();
        assertThat(registro.getTipo()).isEqualTo(TipoEmail.CONFIRMACAO);
        assertThat(registro.isSucesso()).isTrue();
        assertThat(registro.getTentativas()).isEqualTo(1);
    }

    @Test
    void enviarConfirmacaoPagamento_deveRegistrarLogDeFalha_semLancarExcecao_quandoResendFalha() {
        Pedido pedido = pedidoDeTeste();
        when(resendClient.enviar(anyString(), anyString(), anyString())).thenReturn(false);

        // RN-26: mesmo com falha no envio, nada aqui deve lançar exceção.
        emailService.enviarConfirmacaoPagamento(pedido);

        ArgumentCaptor<PedidoEmailLog> captor = ArgumentCaptor.forClass(PedidoEmailLog.class);
        verify(emailLogRepository).save(captor.capture());
        assertThat(captor.getValue().isSucesso()).isFalse();
    }

    @Test
    void tentarReenviarConfirmacoesPendentes_deveAtualizarLogExistente_naoCriarNovo() {
        Pedido pedido = pedidoDeTeste();
        PedidoEmailLog logAntigo = new PedidoEmailLog();
        logAntigo.setPedido(pedido);
        logAntigo.setTipo(TipoEmail.CONFIRMACAO);
        logAntigo.setSucesso(false);
        logAntigo.setTentativas(1);

        when(emailLogRepository.findByTipoAndSucessoFalseAndTentativasAndCriadoEmBefore(
                eq(TipoEmail.CONFIRMACAO), eq(1), any(LocalDateTime.class)))
                .thenReturn(List.of(logAntigo));
        when(resendClient.enviar(anyString(), anyString(), anyString())).thenReturn(true);

        emailService.tentarReenviarConfirmacoesPendentes();

        assertThat(logAntigo.isSucesso()).isTrue();
        assertThat(logAntigo.getTentativas()).isEqualTo(2);
        // Não deve ter criado um registro NOVO — só atualizou o existente em memória.
        verify(emailLogRepository, never()).save(any());
    }

    @Test
    void enviarPedidoEnviado_deveRegistrarTipoCorreto() {
        Pedido pedido = pedidoDeTeste();
        pedido.setCodigoRastreio("BR123456789");
        when(resendClient.enviar(anyString(), anyString(), anyString())).thenReturn(true);

        emailService.enviarPedidoEnviado(pedido);

        ArgumentCaptor<PedidoEmailLog> captor = ArgumentCaptor.forClass(PedidoEmailLog.class);
        verify(emailLogRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoEmail.ENVIADO);
    }

    @Test
    void enviarPedidoCancelado_deveRegistrarTipoCorreto_eIncluirMotivoNoHtml() {
        Pedido pedido = pedidoDeTeste();
        when(resendClient.enviar(anyString(), anyString(), anyString())).thenReturn(true);

        emailService.enviarPedidoCancelado(pedido, "Cliente desistiu");

        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
        verify(resendClient).enviar(anyString(), anyString(), htmlCaptor.capture());
        assertThat(htmlCaptor.getValue()).contains("Cliente desistiu");

        ArgumentCaptor<PedidoEmailLog> logCaptor = ArgumentCaptor.forClass(PedidoEmailLog.class);
        verify(emailLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getTipo()).isEqualTo(TipoEmail.CANCELADO);
    }
}

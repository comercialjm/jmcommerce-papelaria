package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.dto.CheckoutDTOs.ItemCheckout;
import com.jmcodestudio.papelaria.dto.CheckoutDTOs.Requisicao;
import com.jmcodestudio.papelaria.dto.FreteDTOs.Opcao;
import com.jmcodestudio.papelaria.dto.PedidoAdminDTOs.AtualizarStatusRequisicao;
import com.jmcodestudio.papelaria.entity.Pedido;
import com.jmcodestudio.papelaria.entity.PedidoItem;
import com.jmcodestudio.papelaria.entity.Produto;
import com.jmcodestudio.papelaria.entity.StatusPedido;
import com.jmcodestudio.papelaria.exception.RegraDeNegocioException;
import com.jmcodestudio.papelaria.repository.PedidoRepository;
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
import static org.mockito.Mockito.*;

class PedidoServiceTest {

    private PedidoRepository pedidoRepository;
    private ProdutoRepository produtoRepository;
    private EmailService emailService;
    private StripeRefundService stripeRefundService;
    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoRepository = Mockito.mock(PedidoRepository.class);
        produtoRepository = Mockito.mock(ProdutoRepository.class);
        emailService = Mockito.mock(EmailService.class);
        stripeRefundService = Mockito.mock(StripeRefundService.class);
        pedidoService = new PedidoService(pedidoRepository, produtoRepository, emailService, stripeRefundService);

        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Produto produto(long id, String nome, String preco, int estoque) {
        Produto p = new Produto();
        p.setId(id);
        p.setNome(nome);
        p.setPreco(new BigDecimal(preco));
        p.setEstoque(estoque);
        p.setAtivo(true);
        return p;
    }

    private Requisicao requisicaoCheckout(long produtoId, int quantidade) {
        return new Requisicao(
                "Cliente Teste", "cliente@teste.com", "(21) 99999-9999",
                "28621350", "Rua Teste", "123", null, "Bairro", "Cidade", "RJ",
                List.of(new ItemCheckout(produtoId, quantidade)),
                new Opcao("PAC", "Correios", new BigDecimal("20.00"), 5)
        );
    }

    @Test
    void criarAguardandoPagamento_deveLancarExcecao_quandoEstoqueInsuficiente() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto(1L, "Caderno", "39.90", 2)));

        assertThatThrownBy(() -> pedidoService.criarAguardandoPagamento(requisicaoCheckout(1L, 5)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("estoque suficiente");

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void criarAguardandoPagamento_deveCalcularTotalComFrete() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto(1L, "Caderno", "39.90", 10)));

        Pedido pedido = pedidoService.criarAguardandoPagamento(requisicaoCheckout(1L, 2));

        assertThat(pedido.getSubtotal()).isEqualByComparingTo("79.80"); // 39.90 * 2
        assertThat(pedido.getTotal()).isEqualByComparingTo("99.80"); // + frete 20.00
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.AGUARDANDO_PAGAMENTO);
    }

    @Test
    void confirmarPagamento_deveSerIdempotente_quandoPedidoJaEstaPago() {
        Pedido pedido = new Pedido();
        pedido.setNumero("PED123");
        pedido.registrarMudancaStatus(StatusPedido.AGUARDANDO_PAGAMENTO, "sistema");
        pedido.registrarMudancaStatus(StatusPedido.PAGO, "sistema"); // já pago

        when(pedidoRepository.findByStripeSessionId("sess_1")).thenReturn(Optional.of(pedido));

        pedidoService.confirmarPagamento("sess_1");

        verifyNoInteractions(produtoRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void confirmarPagamento_deveDebitarEstoqueEEnviarEmail_quandoAguardandoPagamento() {
        Pedido pedido = new Pedido();
        pedido.setNumero("PED123");
        pedido.registrarMudancaStatus(StatusPedido.AGUARDANDO_PAGAMENTO, "sistema");

        PedidoItem item = new PedidoItem();
        item.setProdutoId(10L);
        item.setQuantidade(2);
        pedido.adicionarItem(item);

        Produto produto = produto(10L, "Caderno", "39.90", 5);

        when(pedidoRepository.findByStripeSessionId("sess_1")).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));

        pedidoService.confirmarPagamento("sess_1");

        assertThat(produto.getEstoque()).isEqualTo(3); // 5 - 2
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGO);
        verify(emailService).enviarConfirmacaoPagamento(pedido);
    }

    @Test
    void atualizarStatusAdmin_deveRejeitarTransicaoInvalida() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.registrarMudancaStatus(StatusPedido.AGUARDANDO_PAGAMENTO, "sistema"); // sem transições manuais

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        AtualizarStatusRequisicao req = new AtualizarStatusRequisicao("EM_PREPARACAO", null, null);

        assertThatThrownBy(() -> pedidoService.atualizarStatusAdmin(1L, req, "admin@loja.com"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void atualizarStatusAdmin_aoCancelarPedidoPago_deveReembolsarERestaurarEstoqueEEnviarEmail() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setStripeSessionId("sess_abc");
        pedido.registrarMudancaStatus(StatusPedido.AGUARDANDO_PAGAMENTO, "sistema");
        pedido.registrarMudancaStatus(StatusPedido.PAGO, "sistema");

        PedidoItem item = new PedidoItem();
        item.setProdutoId(10L);
        item.setQuantidade(2);
        pedido.adicionarItem(item);

        Produto produto = produto(10L, "Caderno", "39.90", 3); // estoque já debitado antes

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));

        AtualizarStatusRequisicao req = new AtualizarStatusRequisicao("CANCELADO", null, "Cliente desistiu");

        pedidoService.atualizarStatusAdmin(1L, req, "admin@loja.com");

        verify(stripeRefundService).reembolsar("sess_abc");
        assertThat(produto.getEstoque()).isEqualTo(5); // 3 + 2 restaurados
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        verify(emailService).enviarPedidoCancelado(pedido, "Cliente desistiu");
    }

    @Test
    void atualizarStatusAdmin_aoEnviar_deveSalvarCodigoRastreioEEnviarEmail() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.registrarMudancaStatus(StatusPedido.AGUARDANDO_PAGAMENTO, "sistema");
        pedido.registrarMudancaStatus(StatusPedido.PAGO, "sistema");
        pedido.registrarMudancaStatus(StatusPedido.EM_PREPARACAO, "admin@loja.com");

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        AtualizarStatusRequisicao req = new AtualizarStatusRequisicao("ENVIADO", "BR123456789", null);

        pedidoService.atualizarStatusAdmin(1L, req, "admin@loja.com");

        assertThat(pedido.getCodigoRastreio()).isEqualTo("BR123456789");
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ENVIADO);
        verify(emailService).enviarPedidoEnviado(pedido);
        verifyNoInteractions(stripeRefundService);
    }

    @Test
    void atualizarStatusAdmin_deveRegistrarHistoricoComEmailDoAdmin() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.registrarMudancaStatus(StatusPedido.AGUARDANDO_PAGAMENTO, "sistema");
        pedido.registrarMudancaStatus(StatusPedido.PAGO, "sistema");

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        pedidoService.atualizarStatusAdmin(1L,
                new AtualizarStatusRequisicao("EM_PREPARACAO", null, null), "admin@loja.com");

        assertThat(pedido.getHistoricoStatus())
                .filteredOn(h -> h.getStatusNovo() == StatusPedido.EM_PREPARACAO)
                .extracting(h -> h.getAlteradoPor())
                .containsExactly("admin@loja.com");
    }
}

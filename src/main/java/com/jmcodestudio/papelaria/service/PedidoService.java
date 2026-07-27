package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.dto.CheckoutDTOs.ItemCheckout;
import com.jmcodestudio.papelaria.dto.CheckoutDTOs.Requisicao;
import com.jmcodestudio.papelaria.dto.PedidoAdminDTOs;
import com.jmcodestudio.papelaria.dto.PedidoAdminDTOs.AtualizarStatusRequisicao;
import com.jmcodestudio.papelaria.dto.PedidoDTOs.Confirmacao;
import com.jmcodestudio.papelaria.dto.PedidoDTOs.ItemResumo;
import com.jmcodestudio.papelaria.entity.*;
import com.jmcodestudio.papelaria.exception.RecursoNaoEncontradoException;
import com.jmcodestudio.papelaria.exception.RegraDeNegocioException;
import com.jmcodestudio.papelaria.repository.PedidoRepository;
import com.jmcodestudio.papelaria.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** UC-08 a UC-11 (checkout) e UC-16 (gestão admin de pedidos). */
@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    // RN-40: transições manuais permitidas pelo admin. AGUARDANDO_PAGAMENTO -> PAGO/EXPIRADO
    // são automáticas (webhook/scheduler) e não entram aqui.
    private static final Map<StatusPedido, Set<StatusPedido>> TRANSICOES_VALIDAS = new EnumMap<>(Map.of(
            StatusPedido.PAGO, Set.of(StatusPedido.EM_PREPARACAO, StatusPedido.CANCELADO),
            StatusPedido.EM_PREPARACAO, Set.of(StatusPedido.ENVIADO, StatusPedido.CANCELADO),
            StatusPedido.ENVIADO, Set.of(StatusPedido.ENTREGUE)
    ));

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final EmailService emailService;
    private final StripeRefundService stripeRefundService;

    /**
     * UC-08, passos 6-8: revalida estoque/preço no servidor (nunca confia no
     * carrinho do navegador) e cria o pedido como AGUARDANDO_PAGAMENTO.
     */
    @Transactional
    public Pedido criarAguardandoPagamento(Requisicao req) {
        Pedido pedido = new Pedido();
        pedido.setNumero(gerarNumero());
        pedido.setClienteNome(req.nomeCompleto());
        pedido.setClienteEmail(req.email());
        pedido.setClienteTelefone(req.telefone());
        pedido.setEnderecoCep(req.cep());
        pedido.setEnderecoRua(req.rua());
        pedido.setEnderecoNumero(req.numero());
        pedido.setEnderecoComplemento(
                (req.complemento() == null || req.complemento().isBlank()) ? null : req.complemento());
        pedido.setEnderecoBairro(req.bairro());
        pedido.setEnderecoCidade(req.cidade());
        pedido.setEnderecoUf(req.uf());
        pedido.setFreteValor(req.frete().preco());
        pedido.setFreteMetodo(req.frete().servico() + " (" + req.frete().transportadora() + ")");

        BigDecimal subtotal = BigDecimal.ZERO;

        for (ItemCheckout itemReq : req.itens()) {
            Produto produto = produtoRepository.findById(itemReq.produtoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Produto não encontrado: id " + itemReq.produtoId()));

            // UC-08, 7a: sem estoque suficiente — bloqueia o pedido inteiro.
            if (!produto.isAtivo() || produto.getEstoque() < itemReq.quantidade()) {
                throw new RegraDeNegocioException(
                        "O produto \"" + produto.getNome() + "\" não possui mais estoque suficiente.");
            }

            PedidoItem item = new PedidoItem();
            item.setProdutoId(produto.getId());
            item.setProdutoNome(produto.getNome());
            item.setQuantidade(itemReq.quantidade());
            item.setPrecoUnitario(produto.getPreco()); // RN-14: preço atual do banco, nunca o do frontend
            pedido.adicionarItem(item);

            subtotal = subtotal.add(produto.getPreco().multiply(BigDecimal.valueOf(itemReq.quantidade())));
        }

        pedido.setSubtotal(subtotal);
        pedido.setTotal(subtotal.add(req.frete().preco()));
        pedido.registrarMudancaStatus(StatusPedido.AGUARDANDO_PAGAMENTO, "sistema");

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public void vincularSessaoStripe(Long pedidoId, String stripeSessionId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado: id " + pedidoId));
        pedido.setStripeSessionId(stripeSessionId);
    }

    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorSessionId(String sessionId) {
        return pedidoRepository.findByStripeSessionId(sessionId);
    }

    /**
     * UC-10: busca e monta a confirmação numa única transação. Buscar o Pedido
     * numa chamada e mapear em outra causaria LazyInitializationException ao
     * acessar pedido.getItens() — a entidade já estaria "desconectada" da sessão
     * original do Hibernate.
     */
    @Transactional(readOnly = true)
    public Optional<Confirmacao> buscarConfirmacaoPorSessionId(String sessionId) {
        return pedidoRepository.findByStripeSessionId(sessionId).map(this::paraConfirmacao);
    }

    /**
     * UC-09, passo 6. RN-24: estoque só debita AQUI, após confirmação via webhook.
     * RN-25: idempotente — se o webhook chegar duplicado, não faz nada na segunda vez.
     */
    @Transactional
    public void confirmarPagamento(String stripeSessionId) {
        Pedido pedido = pedidoRepository.findByStripeSessionId(stripeSessionId).orElse(null);

        if (pedido == null) {
            log.warn("Webhook do Stripe recebido para uma sessão sem pedido correspondente: {}", stripeSessionId);
            return;
        }

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            log.info("Pedido {} já estava em status {} — ignorando webhook duplicado.",
                    pedido.getNumero(), pedido.getStatus());
            return;
        }

        for (PedidoItem item : pedido.getItens()) {
            produtoRepository.findById(item.getProdutoId()).ifPresent(produto -> {
                int novoEstoque = Math.max(0, produto.getEstoque() - item.getQuantidade());
                produto.setEstoque(novoEstoque);
            });
        }

        pedido.registrarMudancaStatus(StatusPedido.PAGO, "sistema");
        log.info("Pedido {} confirmado como PAGO via webhook Stripe.", pedido.getNumero());

        // UC-E1: RN-26 garante que uma falha aqui nunca derruba esta transação.
        emailService.enviarConfirmacaoPagamento(pedido);
    }

    /** RN-20: pedidos não pagos em 30 minutos expiram automaticamente. */
    @Transactional
    public void expirarPedidosAntigos() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(30);
        List<Pedido> pendentes = pedidoRepository
                .findByStatusAndCriadoEmBefore(StatusPedido.AGUARDANDO_PAGAMENTO, limite);

        for (Pedido pedido : pendentes) {
            pedido.registrarMudancaStatus(StatusPedido.EXPIRADO, "sistema");
        }

        if (!pendentes.isEmpty()) {
            log.info("{} pedido(s) expirado(s) automaticamente.", pendentes.size());
        }
    }

    /** UC-16a: listagem do admin, com filtro por status e busca por número/cliente. */
    @Transactional(readOnly = true)
    public Page<PedidoAdminDTOs.ResumoAdmin> listarParaAdmin(StatusPedido status, String busca, Pageable pageable) {
        return pedidoRepository.buscarParaAdmin(status, busca, pageable).map(p -> new PedidoAdminDTOs.ResumoAdmin(
                p.getId(), p.getNumero(), p.getClienteNome(), p.getTotal(), p.getStatus().name(), p.getCriadoEm()
        ));
    }

    /** UC-16b: tudo numa única transação (mesma lição do LazyInitializationException do UC-10). */
    @Transactional(readOnly = true)
    public PedidoAdminDTOs.DetalheAdmin buscarDetalheAdmin(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado: id " + id));

        List<PedidoAdminDTOs.ItemDetalhe> itens = pedido.getItens().stream()
                .map(i -> new PedidoAdminDTOs.ItemDetalhe(
                        i.getProdutoNome(), i.getQuantidade(), i.getPrecoUnitario(),
                        i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade()))))
                .toList();

        List<PedidoAdminDTOs.HistoricoStatus> historico = pedido.getHistoricoStatus().stream()
                .map(h -> new PedidoAdminDTOs.HistoricoStatus(
                        h.getStatusAnterior() != null ? h.getStatusAnterior().name() : null,
                        h.getStatusNovo().name(), h.getAlteradoPor(), h.getCriadoEm()))
                .toList();

        List<PedidoAdminDTOs.EmailLog> emailLogs = pedido.getEmailLogs().stream()
                .map(e -> new PedidoAdminDTOs.EmailLog(e.getTipo().name(), e.isSucesso(), e.getTentativas(), e.getCriadoEm()))
                .toList();

        Set<StatusPedido> proximos = TRANSICOES_VALIDAS.getOrDefault(pedido.getStatus(), Set.of());

        return new PedidoAdminDTOs.DetalheAdmin(
                pedido.getId(), pedido.getNumero(), pedido.getStatus().name(),
                proximos.stream().map(Enum::name).toList(),
                pedido.getClienteNome(), pedido.getClienteEmail(), pedido.getClienteTelefone(),
                montarEnderecoCompleto(pedido),
                itens, pedido.getSubtotal(), pedido.getFreteValor(), pedido.getFreteMetodo(), pedido.getTotal(),
                pedido.getCodigoRastreio(), historico, emailLogs
        );
    }

    /**
     * UC-16c: valida a transição (RN-40), aplica efeitos colaterais por status
     * (reembolso + estoque no cancelamento — RN-41/RN-42) e dispara o e-mail
     * correspondente (UC-E2/E3).
     */
    @Transactional
    public void atualizarStatusAdmin(Long id, AtualizarStatusRequisicao req, String adminEmail) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado: id " + id));

        StatusPedido novoStatus;
        try {
            novoStatus = StatusPedido.valueOf(req.novoStatus());
        } catch (IllegalArgumentException e) {
            throw new RegraDeNegocioException("Status inválido: " + req.novoStatus());
        }

        Set<StatusPedido> permitidos = TRANSICOES_VALIDAS.getOrDefault(pedido.getStatus(), Set.of());
        if (!permitidos.contains(novoStatus)) {
            throw new RegraDeNegocioException(
                    "Não é possível mudar de " + pedido.getStatus() + " para " + novoStatus + " diretamente.");
        }

        if (novoStatus == StatusPedido.ENVIADO && req.codigoRastreio() != null && !req.codigoRastreio().isBlank()) {
            pedido.setCodigoRastreio(req.codigoRastreio());
        }

        if (novoStatus == StatusPedido.CANCELADO) {
            // RN-41: reembolso via Stripe
            if (pedido.getStripeSessionId() != null) {
                stripeRefundService.reembolsar(pedido.getStripeSessionId());
            }
            // RN-42: restaura estoque
            for (PedidoItem item : pedido.getItens()) {
                produtoRepository.findById(item.getProdutoId()).ifPresent(produto ->
                        produto.setEstoque(produto.getEstoque() + item.getQuantidade()));
            }
        }

        pedido.registrarMudancaStatus(novoStatus, adminEmail);

        if (novoStatus == StatusPedido.ENVIADO) {
            emailService.enviarPedidoEnviado(pedido); // UC-E2
        } else if (novoStatus == StatusPedido.CANCELADO) {
            emailService.enviarPedidoCancelado(pedido, req.motivoCancelamento()); // UC-E3 (RN-28 já garantido: só chega aqui pedido PAGO/EM_PREPARACAO)
        }

        log.info("Pedido {} mudou de status para {} (por {}).", pedido.getNumero(), novoStatus, adminEmail);
    }

    private String montarEnderecoCompleto(Pedido pedido) {
        return "%s, %s%s - %s, %s/%s - CEP %s".formatted(
                pedido.getEnderecoRua(), pedido.getEnderecoNumero(),
                pedido.getEnderecoComplemento() != null ? " (" + pedido.getEnderecoComplemento() + ")" : "",
                pedido.getEnderecoBairro(), pedido.getEnderecoCidade(), pedido.getEnderecoUf(),
                pedido.getEnderecoCep());
    }

    @Transactional(readOnly = true)
    public Confirmacao paraConfirmacao(Pedido pedido) {
        List<ItemResumo> itens = pedido.getItens().stream()
                .map(i -> new ItemResumo(
                        i.getProdutoNome(), i.getQuantidade(), i.getPrecoUnitario(),
                        i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade()))))
                .toList();

        return new Confirmacao(
                pedido.getNumero(), pedido.getStatus().name(), itens, montarEnderecoCompleto(pedido),
                pedido.getSubtotal(), pedido.getFreteValor(), pedido.getFreteMetodo(), pedido.getTotal()
        );
    }

    private String gerarNumero() {
        String data = LocalDateTime.now().toLocalDate().toString().replace("-", "");
        String sufixo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "PED" + data + "-" + sufixo;
    }
}

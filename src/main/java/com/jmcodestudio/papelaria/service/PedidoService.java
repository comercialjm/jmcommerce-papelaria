package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.dto.CheckoutDTOs.ItemCheckout;
import com.jmcodestudio.papelaria.dto.CheckoutDTOs.Requisicao;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** UC-08 a UC-11: criação, confirmação e expiração de pedidos. */
@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

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
        pedido.setEnderecoComplemento(req.complemento());
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

        // TODO (M7): disparar e-mail de "Pagamento Confirmado" (UC-E1)
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

    @Transactional(readOnly = true)
    public Confirmacao paraConfirmacao(Pedido pedido) {
        List<ItemResumo> itens = pedido.getItens().stream()
                .map(i -> new ItemResumo(
                        i.getProdutoNome(), i.getQuantidade(), i.getPrecoUnitario(),
                        i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade()))))
                .toList();

        String endereco = "%s, %s%s - %s, %s/%s - CEP %s".formatted(
                pedido.getEnderecoRua(), pedido.getEnderecoNumero(),
                pedido.getEnderecoComplemento() != null ? " (" + pedido.getEnderecoComplemento() + ")" : "",
                pedido.getEnderecoBairro(), pedido.getEnderecoCidade(), pedido.getEnderecoUf(),
                pedido.getEnderecoCep());

        return new Confirmacao(
                pedido.getNumero(), pedido.getStatus().name(), itens, endereco,
                pedido.getSubtotal(), pedido.getFreteValor(), pedido.getFreteMetodo(), pedido.getTotal()
        );
    }

    private String gerarNumero() {
        String data = LocalDateTime.now().toLocalDate().toString().replace("-", "");
        String sufixo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "PED" + data + "-" + sufixo;
    }
}

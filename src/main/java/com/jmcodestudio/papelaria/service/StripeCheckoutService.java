package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.config.StripeProperties;
import com.jmcodestudio.papelaria.entity.Pedido;
import com.jmcodestudio.papelaria.entity.PedidoItem;
import com.jmcodestudio.papelaria.exception.ServicoExternoIndisponivelException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * UC-08, passos 9-10: cria a Checkout Session hospedada do Stripe (RN-19 — nenhum
 * dado de cartão passa pelo nosso servidor).
 */
@Service
@RequiredArgsConstructor
public class StripeCheckoutService {

    private final StripeProperties propriedades;

    @PostConstruct
    void configurarChaveApi() {
        Stripe.apiKey = propriedades.secretKey();
    }

    public Session criarSessao(Pedido pedido) {
        SessionCreateParams.Builder params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(propriedades.appBaseUrl() + "/pedido/confirmado?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(propriedades.appBaseUrl() + "/pedido/cancelado")
                .setCustomerEmail(pedido.getClienteEmail())
                .putMetadata("pedido_numero", pedido.getNumero());

        for (PedidoItem item : pedido.getItens()) {
            params.addLineItem(criarLineItem(item.getProdutoNome(), item.getPrecoUnitario(), item.getQuantidade()));
        }

        // Frete como um item de linha adicional, para o cliente ver discriminado no Stripe.
        if (pedido.getFreteValor().compareTo(BigDecimal.ZERO) > 0) {
            params.addLineItem(criarLineItem(
                    "Frete (" + pedido.getFreteMetodo() + ")", pedido.getFreteValor(), 1));
        }

        try {
            return Session.create(params.build());
        } catch (StripeException e) {
            throw new ServicoExternoIndisponivelException(
                    "Erro ao processar pagamento. Tente novamente.", e);
        }
    }

    private SessionCreateParams.LineItem criarLineItem(String nome, BigDecimal precoUnitario, int quantidade) {
        long precoEmCentavos = precoUnitario.multiply(BigDecimal.valueOf(100)).longValueExact();

        return SessionCreateParams.LineItem.builder()
                .setQuantity((long) quantidade)
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("brl")
                                .setUnitAmount(precoEmCentavos)
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(nome)
                                                .build()
                                )
                                .build()
                )
                .build();
    }
}

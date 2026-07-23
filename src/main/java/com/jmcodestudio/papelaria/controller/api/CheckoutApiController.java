package com.jmcodestudio.papelaria.controller.api;

import com.jmcodestudio.papelaria.dto.CheckoutDTOs.Requisicao;
import com.jmcodestudio.papelaria.dto.CheckoutDTOs.Resposta;
import com.jmcodestudio.papelaria.entity.Pedido;
import com.jmcodestudio.papelaria.service.PedidoService;
import com.jmcodestudio.papelaria.service.StripeCheckoutService;
import com.stripe.model.checkout.Session;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** UC-08: cria o pedido (AGUARDANDO_PAGAMENTO) e a sessão de pagamento do Stripe. */
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutApiController {

    private final PedidoService pedidoService;
    private final StripeCheckoutService stripeCheckoutService;

    @PostMapping
    public Resposta finalizar(@Valid @RequestBody Requisicao requisicao) {
        Pedido pedido = pedidoService.criarAguardandoPagamento(requisicao);
        Session sessao = stripeCheckoutService.criarSessao(pedido);
        pedidoService.vincularSessaoStripe(pedido.getId(), sessao.getId());
        return new Resposta(sessao.getUrl());
    }

}

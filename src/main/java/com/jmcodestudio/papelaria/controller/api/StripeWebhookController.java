package com.jmcodestudio.papelaria.controller.api;

import com.jmcodestudio.papelaria.config.StripeProperties;
import com.jmcodestudio.papelaria.service.PedidoService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UC-09, passos 5-6: recebe o webhook do Stripe confirmando o pagamento.
 *
 * IMPORTANTE: o corpo precisa chegar exatamente como o Stripe enviou (raw String),
 * sem passar por nenhuma desserialização JSON antes da verificação de assinatura
 * (RN-23) — por isso o parâmetro é String, não um DTO.
 */
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final StripeProperties propriedades;
    private final PedidoService pedidoService;

    @PostMapping("/stripe")
    public ResponseEntity<String> receber(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String assinatura
    ) {
        Event evento;
        try {
            evento = Webhook.constructEvent(payload, assinatura, propriedades.webhookSecret());
        } catch (SignatureVerificationException e) {
            log.warn("Webhook do Stripe com assinatura inválida — possível tentativa de fraude.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Assinatura inválida");
        }

        if ("checkout.session.completed".equals(evento.getType())) {
            EventDataObjectDeserializer dataObjectDeserializer = evento.getDataObjectDeserializer();
            dataObjectDeserializer.getObject().ifPresent(objeto -> {
                if (objeto instanceof Session sessao) {
                    // RN-25: confirmarPagamento já é idempotente por si só.
                    pedidoService.confirmarPagamento(sessao.getId());
                }
            });
        }

        // Qualquer outro tipo de evento é reconhecido mas ignorado (200 evita reenvios desnecessários).
        return ResponseEntity.ok("Recebido");
    }

}

package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.exception.ServicoExternoIndisponivelException;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** RN-41: solicita reembolso via API do Stripe ao cancelar um pedido já pago. */
@Service
public class StripeRefundService {

    private static final Logger log = LoggerFactory.getLogger(StripeRefundService.class);

    public void reembolsar(String stripeSessionId) {
        try {
            Session session = Session.retrieve(stripeSessionId);
            String paymentIntentId = session.getPaymentIntent();

            if (paymentIntentId == null) {
                log.warn("Sessão {} não tem payment_intent — nada a reembolsar (talvez nunca foi pago).",
                        stripeSessionId);
                return;
            }

            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();

            Refund.create(params);
            log.info("Reembolso solicitado com sucesso para a sessão {}.", stripeSessionId);

        } catch (StripeException e) {
            throw new ServicoExternoIndisponivelException(
                    "Não foi possível processar o reembolso no Stripe. Tente novamente.", e);
        }
    }
}

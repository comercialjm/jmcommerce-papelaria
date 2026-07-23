package com.jmcodestudio.papelaria.entity;

/**
 * Fluxo de status do pedido (UC-16c):
 * AGUARDANDO_PAGAMENTO → PAGO (automático via webhook Stripe)
 * AGUARDANDO_PAGAMENTO → EXPIRADO (automático após 30 min)
 * PAGO → EM_PREPARACAO (manual, admin)
 * EM_PREPARACAO → ENVIADO (manual, admin)
 * ENVIADO → ENTREGUE (manual, admin)
 * PAGO/EM_PREPARACAO → CANCELADO (manual, admin)
 */
public enum StatusPedido {
    AGUARDANDO_PAGAMENTO,
    PAGO,
    EXPIRADO,
    EM_PREPARACAO,
    ENVIADO,
    ENTREGUE,
    CANCELADO
}

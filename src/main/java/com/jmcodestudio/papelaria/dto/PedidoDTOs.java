package com.jmcodestudio.papelaria.dto;

import java.math.BigDecimal;
import java.util.List;

public class PedidoDTOs {

    public record ItemResumo(
            String nome,
            Integer quantidade,
            BigDecimal precoUnitario,
            BigDecimal subtotalItem
    ) {}

    /** UC-10: dados exibidos na página de confirmação do pedido. */
    public record Confirmacao(
            String numero,
            String status,       // "PAGO" ou "AGUARDANDO_PAGAMENTO" (UC-10, 2b)
            List<ItemResumo> itens,
            String enderecoCompleto,
            BigDecimal subtotal,
            BigDecimal freteValor,
            String freteMetodo,
            BigDecimal total
    ) {}
}

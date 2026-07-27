package com.jmcodestudio.papelaria.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoAdminDTOs {

    /** UC-16a: linha da listagem. */
    public record ResumoAdmin(
            Long id,
            String numero,
            String clienteNome,
            BigDecimal total,
            String status,
            LocalDateTime criadoEm
    ) {}

    public record ItemDetalhe(
            String produtoNome,
            Integer quantidade,
            BigDecimal precoUnitario,
            BigDecimal subtotal
    ) {}

    public record HistoricoStatus(
            String statusAnterior,
            String statusNovo,
            String alteradoPor,
            LocalDateTime criadoEm
    ) {}

    public record EmailLog(
            String tipo,
            boolean sucesso,
            Integer tentativas,
            LocalDateTime criadoEm
    ) {}

    /** UC-16b: detalhe completo do pedido. */
    public record DetalheAdmin(
            Long id,
            String numero,
            String status,
            List<String> proximosStatusPossiveis,

            String clienteNome,
            String clienteEmail,
            String clienteTelefone,

            String enderecoCompleto,

            List<ItemDetalhe> itens,
            BigDecimal subtotal,
            BigDecimal freteValor,
            String freteMetodo,
            BigDecimal total,

            String codigoRastreio,

            List<HistoricoStatus> historico,
            List<EmailLog> emailLogs
    ) {}

    /** UC-16c: requisição de mudança de status. */
    public record AtualizarStatusRequisicao(
            @NotNull String novoStatus,
            String codigoRastreio,   // opcional, usado quando novoStatus = ENVIADO
            String motivoCancelamento // opcional, usado quando novoStatus = CANCELADO
    ) {}
}

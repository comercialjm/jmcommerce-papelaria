package com.jmcodestudio.papelaria.dto;

import java.util.List;
import java.util.Map;

public class DashboardDTOs {

    /** UC-13: números gerais para o painel principal do admin. */
    public record Resumo(
            long totalProdutos,
            long totalProdutosEsgotados,
            Map<String, Long> pedidosPorStatus,
            long pedidosPendentesEnvio, // RN-33: destaque visual (PAGO + EM_PREPARACAO)
            List<PedidoAdminDTOs.ResumoAdmin> pedidosRecentes
    ) {}
}

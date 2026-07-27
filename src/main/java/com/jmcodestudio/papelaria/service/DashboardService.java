package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.dto.DashboardDTOs.Resumo;
import com.jmcodestudio.papelaria.dto.PedidoAdminDTOs.ResumoAdmin;
import com.jmcodestudio.papelaria.entity.Pedido;
import com.jmcodestudio.papelaria.entity.StatusPedido;
import com.jmcodestudio.papelaria.repository.PedidoRepository;
import com.jmcodestudio.papelaria.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** UC-13: números do dashboard do admin. */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;

    @Transactional(readOnly = true)
    public Resumo montar() {
        long totalProdutos = produtoRepository.count();
        long totalEsgotados = produtoRepository.countByAtivoTrueAndEstoque(0);

        Map<String, Long> pedidosPorStatus = new LinkedHashMap<>();
        for (StatusPedido status : StatusPedido.values()) {
            pedidosPorStatus.put(status.name(), pedidoRepository.countByStatus(status));
        }

        // RN-33: pedidos pendentes de envio precisam de destaque visual
        long pendentesEnvio = pedidoRepository.countByStatus(StatusPedido.PAGO)
                + pedidoRepository.countByStatus(StatusPedido.EM_PREPARACAO);

        List<ResumoAdmin> recentes = pedidoRepository.findTop10ByOrderByCriadoEmDesc().stream()
                .map(this::paraResumoAdmin)
                .toList();

        return new Resumo(totalProdutos, totalEsgotados, pedidosPorStatus, pendentesEnvio, recentes);
    }

    private ResumoAdmin paraResumoAdmin(Pedido p) {
        return new ResumoAdmin(p.getId(), p.getNumero(), p.getClienteNome(), p.getTotal(),
                p.getStatus().name(), p.getCriadoEm());
    }
}

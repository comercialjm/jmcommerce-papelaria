package com.jmcodestudio.papelaria.scheduler;

import com.jmcodestudio.papelaria.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** RN-20: roda a cada 5 minutos e expira pedidos parados em AGUARDANDO_PAGAMENTO há 30+ min. */
@Component
@RequiredArgsConstructor
public class ExpiracaoPedidoScheduler {

    private final PedidoService pedidoService;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void expirarPedidosAntigos() {
        pedidoService.expirarPedidosAntigos();
    }

}

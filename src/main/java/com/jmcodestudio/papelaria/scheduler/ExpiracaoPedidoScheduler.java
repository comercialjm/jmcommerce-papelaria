package com.jmcodestudio.papelaria.scheduler;

import com.jmcodestudio.papelaria.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** RN-20: roda a cada 5 minutos e expira pedidos parados em AGUARDANDO_PAGAMENTO há 30+ min. */
@Component
@RequiredArgsConstructor
public class ExpiracaoPedidoScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpiracaoPedidoScheduler.class);

    private final PedidoService pedidoService;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void expirarPedidosAntigos() {
        try {
            pedidoService.expirarPedidosAntigos();
        } catch (Exception e) {
            // Uma falha aqui (ex: hiccup momentâneo do banco) nunca deve impedir
            // a próxima execução agendada — só registra e segue.
            log.error("Falha ao expirar pedidos antigos. Tentando de novo no próximo ciclo.", e);
        }
    }

}

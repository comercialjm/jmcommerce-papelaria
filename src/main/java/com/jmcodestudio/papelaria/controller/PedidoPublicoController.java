package com.jmcodestudio.papelaria.controller;

import com.jmcodestudio.papelaria.dto.PedidoDTOs;
import com.jmcodestudio.papelaria.entity.StatusPedido;
import com.jmcodestudio.papelaria.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PedidoPublicoController {

    private final PedidoService pedidoService;

    /** UC-10: confirmação pós-pagamento. */
    @GetMapping("/pedido/confirmado")
    public String confirmado(@RequestParam("session_id") String sessionId, Model model) {
        Optional<PedidoDTOs.Confirmacao> confirmacaoOpt = pedidoService.buscarConfirmacaoPorSessionId(sessionId);

        // UC-10, 2a: session_id inválida ou pedido não encontrado
        if (confirmacaoOpt.isEmpty()) {
            model.addAttribute("encontrado", false);
            model.addAttribute("aindaProcessando", false);
            return "pedido-confirmado";
        }

        PedidoDTOs.Confirmacao confirmacao = confirmacaoOpt.get();
        boolean aindaProcessando = StatusPedido.AGUARDANDO_PAGAMENTO.name().equals(confirmacao.status());

        model.addAttribute("encontrado", true);
        model.addAttribute("aindaProcessando", aindaProcessando);

        // UC-10, 2b: webhook ainda não chegou — não expõe dados do pedido ainda
        if (!aindaProcessando) {
            model.addAttribute("pedido", confirmacao);
        }

        return "pedido-confirmado";
    }

    /** UC-11: cliente desistiu do pagamento no Stripe. */
    @GetMapping("/pedido/cancelado")
    public String cancelado() {
        return "pedido-cancelado";
    }

}

package com.jmcodestudio.papelaria.controller.api.admin;

import com.jmcodestudio.papelaria.dto.PedidoAdminDTOs.AtualizarStatusRequisicao;
import com.jmcodestudio.papelaria.dto.PedidoAdminDTOs.DetalheAdmin;
import com.jmcodestudio.papelaria.dto.PedidoAdminDTOs.ResumoAdmin;
import com.jmcodestudio.papelaria.entity.StatusPedido;
import com.jmcodestudio.papelaria.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/** UC-16: gestão de pedidos pelo admin. */
@RestController
@RequestMapping("/admin/api/pedidos")
@RequiredArgsConstructor
public class PedidoAdminApiController {

    private final PedidoService pedidoService;

    @GetMapping
    public Page<ResumoAdmin> listar(
            @RequestParam(required = false) StatusPedido status,
            @RequestParam(required = false) String busca,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return pedidoService.listarParaAdmin(status, busca, pageable);
    }

    @GetMapping("/{id}")
    public DetalheAdmin detalhe(@PathVariable Long id) {
        return pedidoService.buscarDetalheAdmin(id);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusRequisicao requisicao,
            Principal principal
    ) {
        pedidoService.atualizarStatusAdmin(id, requisicao, principal.getName());
        return ResponseEntity.noContent().build();
    }

}

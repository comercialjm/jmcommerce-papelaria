package com.jmcodestudio.papelaria.controller.api.admin;

import com.jmcodestudio.papelaria.dto.ProdutoDTOs.Detalhe;
import com.jmcodestudio.papelaria.dto.ProdutoDTOs.Formulario;
import com.jmcodestudio.papelaria.dto.ProdutoDTOs.Resumo;
import com.jmcodestudio.papelaria.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CRUD de produtos do painel admin (UC-14).
 *
 * TODO (M8): proteger este controller com Spring Security exigindo
 * ROLE_ADMIN — hoje está liberado via SecurityConfig#permitAll para
 * permitir o desenvolvimento do M3/M4 sem travar em login.
 */
@RestController
@RequestMapping("/admin/api/produtos")
@RequiredArgsConstructor
public class ProdutoAdminApiController {

    private final ProdutoService produtoService;

    @GetMapping
    public Page<Resumo> listar(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return produtoService.listarParaAdmin(nome, pageable);
    }

    @GetMapping("/{id}")
    public Detalhe detalhe(@PathVariable Long id) {
        return produtoService.buscarDetalheAdmin(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Detalhe criar(@Valid @RequestBody Formulario form) {
        return produtoService.criar(form);
    }

    @PutMapping("/{id}")
    public Detalhe atualizar(@PathVariable Long id, @Valid @RequestBody Formulario form) {
        return produtoService.atualizar(id, form);
    }

    /** UC-14d: nunca exclui (RN-26/RN-34) — apenas ativa/desativa. */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterarStatus(@PathVariable Long id, @RequestParam boolean ativo) {
        produtoService.alterarStatus(id, ativo);
        return ResponseEntity.noContent().build();
    }
}

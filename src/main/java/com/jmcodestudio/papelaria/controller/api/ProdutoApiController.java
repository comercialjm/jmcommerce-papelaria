package com.jmcodestudio.papelaria.controller.api;

import com.jmcodestudio.papelaria.dto.ProdutoDTOs.Detalhe;
import com.jmcodestudio.papelaria.dto.ProdutoDTOs.Resumo;
import com.jmcodestudio.papelaria.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * API pública de produtos. Consumida hoje pelo carrinho em localStorage
 * (UC-06, para revalidar preço/estoque atual) e, no M4, pelas páginas
 * públicas de catálogo e detalhe.
 */
@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoApiController {

    private final ProdutoService produtoService;

    @GetMapping
    public Page<Resumo> listar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 12) Pageable pageable
    ) {
        if (q != null && !q.isBlank()) {
            return produtoService.buscar(q, pageable); // UC-04
        }
        return produtoService.listarCatalogo(categoriaId, pageable); // UC-02
    }

    @GetMapping("/{id}")
    public Detalhe detalhe(@PathVariable Long id) {
        return produtoService.buscarDetalhePublico(id); // UC-03
    }
}

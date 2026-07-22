package com.jmcodestudio.papelaria.controller;

import com.jmcodestudio.papelaria.dto.CategoriaDTOs.Resposta;
import com.jmcodestudio.papelaria.dto.ProdutoDTOs.Resumo;
import com.jmcodestudio.papelaria.service.CategoriaService;
import com.jmcodestudio.papelaria.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** UC-02 (catálogo), UC-04 (busca) — mesma página pública, /produtos. */
@Controller
@RequiredArgsConstructor
public class CatalogoController {

    private static final int ITENS_POR_PAGINA = 12;

    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;

    @GetMapping("/produtos")
    public String catalogo(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "recentes") String ordenar,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, ITENS_POR_PAGINA, resolverOrdenacao(ordenar));

        boolean buscando = q != null && !q.isBlank();
        Page<Resumo> produtos = buscando
                ? produtoService.buscar(q.trim(), pageable)
                : produtoService.listarCatalogo(categoriaId, pageable);

        List<Resposta> categorias = categoriaService.listarParaCatalogoPublico();
        String nomeCategoriaSelecionada = categorias.stream()
                .filter(c -> c.id().equals(categoriaId))
                .map(Resposta::nome)
                .findFirst()
                .orElse(null);

        model.addAttribute("produtos", produtos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("nomeCategoriaSelecionada", nomeCategoriaSelecionada);
        model.addAttribute("q", buscando ? q.trim() : null);
        model.addAttribute("ordenar", ordenar);

        return "catalogo";
    }

    /** UC-02, passo 4: preço (menor/maior), nome (A-Z), mais recentes. */
    private Sort resolverOrdenacao(String ordenar) {
        return switch (ordenar) {
            case "preco_asc" -> Sort.by("preco").ascending();
            case "preco_desc" -> Sort.by("preco").descending();
            case "nome" -> Sort.by("nome").ascending();
            default -> Sort.by("criadoEm").descending(); // "recentes"
        };
    }

}

package com.jmcodestudio.papelaria.controller;

import com.jmcodestudio.papelaria.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * UC-03: página de detalhe do produto.
 *
 * Se o produto não existir ou estiver inativo, ProdutoService.buscarDetalhePublico
 * lança RecursoNaoEncontradoException (anotada com @ResponseStatus NOT_FOUND), que
 * o Spring resolve automaticamente para templates/error/404.html (UC-03, fluxo 2b).
 */
@Controller
@RequiredArgsConstructor
public class ProdutoPublicoController {

    private final ProdutoService produtoService;

    @GetMapping("/produto/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("produto", produtoService.buscarDetalhePublico(id));
        return "produto";
    }

}

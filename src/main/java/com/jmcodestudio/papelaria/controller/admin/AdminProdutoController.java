package com.jmcodestudio.papelaria.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * UC-14: só serve as páginas (Thymeleaf). Toda a lógica de CRUD em si já existe
 * desde o M3 em ProdutoAdminApiController — estas telas consomem aquela API
 * via JavaScript, no mesmo padrão do carrinho/checkout.
 */
@Controller
public class AdminProdutoController {

    @GetMapping("/admin/produtos")
    public String lista() {
        return "admin/produtos-lista";
    }

    @GetMapping("/admin/produtos/novo")
    public String novo(Model model) {
        model.addAttribute("modo", "criar");
        model.addAttribute("produtoId", null);
        return "admin/produtos-form";
    }

    @GetMapping("/admin/produtos/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("modo", "editar");
        model.addAttribute("produtoId", id);
        return "admin/produtos-form";
    }

}

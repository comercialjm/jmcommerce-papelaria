package com.jmcodestudio.papelaria.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** UC-16: as páginas em si — toda a lógica já está em PedidoAdminApiController. */
@Controller
public class AdminPedidoController {

    @GetMapping("/admin/pedidos")
    public String lista() {
        return "admin/pedidos-lista";
    }

    @GetMapping("/admin/pedidos/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("pedidoId", id);
        return "admin/pedidos-detalhe";
    }

}

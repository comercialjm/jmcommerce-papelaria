package com.jmcodestudio.papelaria.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** UC-15: página única (lista + formulário) — a API já existe desde o M3. */
@Controller
public class AdminCategoriaController {

    @GetMapping("/admin/categorias")
    public String categorias() {
        return "admin/categorias";
    }

}

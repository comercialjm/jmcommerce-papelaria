package com.jmcodestudio.papelaria.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * UC-12: só serve a tela do formulário (GET). O POST em /admin/login é
 * interceptado pelo filtro de autenticação do Spring Security, nunca chega
 * a este controller.
 */
@Controller
public class AdminAuthController {

    @GetMapping("/admin/login")
    public String login() {
        return "admin/login";
    }

}

package com.jmcodestudio.papelaria.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminConfiguracaoController {

    @GetMapping("/admin/configuracoes")
    public String configuracoes() {
        return "admin/configuracoes";
    }

}

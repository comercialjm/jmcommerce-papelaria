package com.jmcodestudio.papelaria.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * UC-06: a página em si é um "casco" — o conteúdo real (itens, preços, total)
 * é montado no navegador a partir do localStorage + /api/carrinho/validar,
 * já que o carrinho não existe no banco de dados (RN-09).
 */
@Controller
public class CarrinhoController {

    @GetMapping("/carrinho")
    public String carrinho() {
        return "carrinho";
    }

}

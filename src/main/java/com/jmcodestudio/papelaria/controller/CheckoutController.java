package com.jmcodestudio.papelaria.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** UC-08: casco da página — dados vêm do localStorage (carrinho + frete escolhido). */
@Controller
public class CheckoutController {

    @GetMapping("/checkout")
    public String checkout() {
        return "checkout";
    }

}

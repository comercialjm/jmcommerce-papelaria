package com.jmcodestudio.papelaria.controller.api;

import com.jmcodestudio.papelaria.dto.CarrinhoDTOs.RequisicaoValidacao;
import com.jmcodestudio.papelaria.dto.CarrinhoDTOs.RespostaValidacao;
import com.jmcodestudio.papelaria.service.CarrinhoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UC-06: chamada pelo JavaScript da página /carrinho para revalidar o conteúdo
 * do localStorage contra os dados reais do banco (preço, estoque, disponibilidade).
 */
@RestController
@RequestMapping("/api/carrinho")
@RequiredArgsConstructor
public class CarrinhoApiController {

    private final CarrinhoService carrinhoService;

    @PostMapping("/validar")
    public RespostaValidacao validar(@Valid @RequestBody RequisicaoValidacao requisicao) {
        return carrinhoService.validar(requisicao);
    }

}

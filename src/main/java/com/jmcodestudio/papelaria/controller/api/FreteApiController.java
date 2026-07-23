package com.jmcodestudio.papelaria.controller.api;

import com.jmcodestudio.papelaria.dto.FreteDTOs.Opcao;
import com.jmcodestudio.papelaria.dto.FreteDTOs.Requisicao;
import com.jmcodestudio.papelaria.service.FreteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** UC-07: chamada pela página do carrinho para calcular opções de frete por CEP. */
@RestController
@RequestMapping("/api/frete")
@RequiredArgsConstructor
public class FreteApiController {

    private final FreteService freteService;

    @PostMapping("/calcular")
    public List<Opcao> calcular(@Valid @RequestBody Requisicao requisicao) {
        return freteService.calcular(requisicao);
    }

}

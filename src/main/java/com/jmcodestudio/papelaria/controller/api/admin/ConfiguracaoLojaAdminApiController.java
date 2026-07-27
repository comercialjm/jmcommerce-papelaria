package com.jmcodestudio.papelaria.controller.api.admin;

import com.jmcodestudio.papelaria.dto.ConfiguracaoLojaDTOs.Detalhe;
import com.jmcodestudio.papelaria.dto.ConfiguracaoLojaDTOs.Formulario;
import com.jmcodestudio.papelaria.service.ConfiguracaoLojaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** UC-17: configurações gerais da loja. */
@RestController
@RequestMapping("/admin/api/configuracoes")
@RequiredArgsConstructor
public class ConfiguracaoLojaAdminApiController {

    private final ConfiguracaoLojaService configuracaoLojaService;

    @GetMapping
    public Detalhe buscar() {
        return configuracaoLojaService.buscarParaAdmin();
    }

    @PutMapping
    public Detalhe atualizar(@Valid @RequestBody Formulario formulario) {
        configuracaoLojaService.atualizar(formulario);
        return configuracaoLojaService.buscarParaAdmin();
    }

}

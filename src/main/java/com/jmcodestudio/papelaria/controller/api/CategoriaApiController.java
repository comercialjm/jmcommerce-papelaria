package com.jmcodestudio.papelaria.controller.api;

import com.jmcodestudio.papelaria.dto.CategoriaDTOs.Resposta;
import com.jmcodestudio.papelaria.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API pública de categorias — usada nos filtros do catálogo (UC-02). */
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaApiController {

    private final CategoriaService categoriaService;

    @GetMapping
    public List<Resposta> listar() {
        return categoriaService.listarParaCatalogoPublico();
    }
}

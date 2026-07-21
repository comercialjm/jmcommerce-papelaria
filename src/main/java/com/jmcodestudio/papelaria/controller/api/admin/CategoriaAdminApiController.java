package com.jmcodestudio.papelaria.controller.api.admin;

import com.jmcodestudio.papelaria.dto.CategoriaDTOs.Formulario;
import com.jmcodestudio.papelaria.dto.CategoriaDTOs.Resposta;
import com.jmcodestudio.papelaria.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD de categorias do painel admin (UC-15).
 *
 * TODO (M8): proteger com Spring Security (ROLE_ADMIN), assim como o
 * ProdutoAdminApiController.
 */
@RestController
@RequestMapping("/admin/api/categorias")
@RequiredArgsConstructor
public class CategoriaAdminApiController {

    private final CategoriaService categoriaService;

    @GetMapping
    public List<Resposta> listar() {
        return categoriaService.listarParaAdmin();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Resposta criar(@Valid @RequestBody Formulario form) {
        return categoriaService.criar(form);
    }

    @PutMapping("/{id}")
    public Resposta atualizar(@PathVariable Long id, @Valid @RequestBody Formulario form) {
        return categoriaService.atualizar(id, form);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterarStatus(@PathVariable Long id, @RequestParam boolean ativa) {
        categoriaService.alterarStatus(id, ativa);
        return ResponseEntity.noContent().build();
    }

    /** RN-36: bloqueado se houver produtos ativos ou subcategorias vinculadas. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

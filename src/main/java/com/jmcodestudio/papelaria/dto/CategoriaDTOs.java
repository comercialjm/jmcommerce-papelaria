package com.jmcodestudio.papelaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CategoriaDTOs {

    /** Retornado pela API. Inclui subcategorias para uso no admin (RN-38). */
    public record Resposta(
            Long id,
            String nome,
            String imagemUrl,
            Long parentId,
            String parentNome,
            boolean ativa,
            long quantidadeProdutos,
            List<Resposta> subcategorias
    ) {}

    /** Enviado pelo admin ao criar ou editar uma categoria (UC-15). */
    public record Formulario(
            @NotBlank(message = "O nome da categoria é obrigatório.")
            @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
            String nome,

            String imagemUrl,

            Long parentId // null = categoria de nível 1
    ) {}
}

package com.jmcodestudio.papelaria.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public class ProdutoDTOs {

    /** Usado no grid do catálogo (UC-02) — só os campos que a listagem precisa. */
    public record Resumo(
            Long id,
            String nome,
            BigDecimal preco,
            String imagemCapa,
            boolean esgotado
    ) {}

    /** Usado na página de detalhe (UC-03) e na edição no admin (UC-14c). */
    public record Detalhe(
            Long id,
            String nome,
            String descricao,
            BigDecimal preco,
            Integer estoque,
            boolean esgotado,
            Long categoriaId,
            String categoriaNome,
            Integer pesoGramas,
            BigDecimal larguraCm,
            BigDecimal alturaCm,
            BigDecimal comprimentoCm,
            boolean ativo,
            List<String> imagens
    ) {}

    /** Enviado pelo admin ao cadastrar ou editar um produto (UC-14b, UC-14c). */
    public record Formulario(
            @NotBlank(message = "O nome do produto é obrigatório.")
            @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
            String nome,

            @NotBlank(message = "A descrição é obrigatória.")
            String descricao,

            @NotNull(message = "O preço é obrigatório.")
            @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
            BigDecimal preco,

            @NotNull(message = "O estoque é obrigatório.")
            @Min(value = 0, message = "O estoque não pode ser negativo.")
            Integer estoque,

            @NotNull(message = "A categoria é obrigatória.")
            Long categoriaId,

            @NotNull(message = "O peso é obrigatório para o cálculo de frete.")
            @Min(value = 1, message = "O peso deve ser maior que zero.")
            Integer pesoGramas,

            @NotNull @DecimalMin(value = "0.1") BigDecimal larguraCm,
            @NotNull @DecimalMin(value = "0.1") BigDecimal alturaCm,
            @NotNull @DecimalMin(value = "0.1") BigDecimal comprimentoCm,

            @Size(min = 1, max = 5, message = "Cadastre de 1 a 5 imagens (RN-03).")
            List<@NotBlank String> imagens
    ) {}
}

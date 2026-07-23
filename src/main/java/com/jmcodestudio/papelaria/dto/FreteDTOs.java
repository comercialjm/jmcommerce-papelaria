package com.jmcodestudio.papelaria.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.List;

public class FreteDTOs {

    public record ItemFrete(
            @NotNull Long produtoId,
            @NotNull Integer quantidade
    ) {}

    public record Requisicao(
            @NotBlank
            @Pattern(regexp = "\\d{8}", message = "CEP inválido. Informe 8 dígitos (ex: 01001000).")
            String cep,

            @NotEmpty @Valid List<ItemFrete> itens
    ) {}

    /** Uma opção de frete pronta para exibição (UC-07, passo 5). */
    public record Opcao(
            String servico,       // ex: "PAC", "SEDEX"
            String transportadora,
            BigDecimal preco,
            Integer prazoDias
    ) {}

    public record Resposta(List<Opcao> opcoes) {}
}

package com.jmcodestudio.papelaria.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public class CheckoutDTOs {

    public record ItemCheckout(
            @NotNull Long produtoId,
            @NotNull @Min(1) Integer quantidade
    ) {}

    /** UC-08: dados pessoais + endereço + itens + frete já escolhido no carrinho. */
    public record Requisicao(
            @NotBlank(message = "Nome completo é obrigatório.")
            @Size(max = 150)
            String nomeCompleto,

            @NotBlank(message = "E-mail é obrigatório.")
            @Email(message = "E-mail inválido.")
            String email,

            @NotBlank(message = "Telefone é obrigatório.")
            @Pattern(regexp = "\\(\\d{2}\\) \\d{4,5}-\\d{4}",
                    message = "Telefone inválido. Use o formato (XX) XXXXX-XXXX.")
            String telefone,

            @NotBlank(message = "CEP é obrigatório.")
            @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido.")
            String cep,

            @NotBlank(message = "Rua é obrigatória.") String rua,
            @NotBlank(message = "Número é obrigatório.") String numero,
            String complemento,
            @NotBlank(message = "Bairro é obrigatório.") String bairro,
            @NotBlank(message = "Cidade é obrigatória.") String cidade,

            @NotBlank @Size(min = 2, max = 2, message = "UF deve ter 2 letras.")
            String uf,

            @NotEmpty(message = "O carrinho está vazio.")
            @Valid
            List<ItemCheckout> itens,

            @NotNull(message = "Selecione uma opção de frete.")
            @Valid
            FreteDTOs.Opcao frete
    ) {}

    public record Resposta(String checkoutUrl) {}
}

package com.jmcodestudio.papelaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ConfiguracaoLojaDTOs {

    /** UC-17: dados atuais, para preencher a tela. */
    public record Detalhe(
            String nomeLoja,
            String cepOrigem,
            String contatoEmail,
            String contatoWhatsapp,
            String bannerImagemUrl,
            String bannerTitulo,
            String bannerSubtitulo,
            String bannerLink
    ) {}

    /**
     * UC-17: formulário de edição. RN-45: propositalmente NÃO tem nenhum campo
     * para o link do rodapé "Desenvolvido por JM Code Studio" — isso é fixo
     * no código, não é dado de configuração da loja.
     */
    public record Formulario(
            @NotBlank(message = "Nome da loja é obrigatório.")
            String nomeLoja,

            @NotBlank(message = "CEP de origem é obrigatório.")
            @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido.")
            String cepOrigem,

            String contatoEmail,
            String contatoWhatsapp,

            // RN-46: banner único, todos os campos opcionais (fallback se vazio)
            String bannerImagemUrl,
            String bannerTitulo,
            String bannerSubtitulo,
            String bannerLink
    ) {}
}

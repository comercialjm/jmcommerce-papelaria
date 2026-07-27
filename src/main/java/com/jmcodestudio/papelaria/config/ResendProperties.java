package com.jmcodestudio.papelaria.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "resend")
public record ResendProperties(
        String apiKey,
        String fromEmail
) {
    private static final String FROM_EMAIL_PADRAO = "Loja de Papelaria <onboarding@resend.dev>";

    // Blinda contra o cenário onde a variável de ambiente existe mas está vazia
    // (nesse caso o valor padrão do application.yml nunca é usado — mesma pegadinha
    // que já vimos com spring.profiles.active).
    public ResendProperties {
        if (fromEmail == null || fromEmail.isBlank()) {
            fromEmail = FROM_EMAIL_PADRAO;
        }
    }
}

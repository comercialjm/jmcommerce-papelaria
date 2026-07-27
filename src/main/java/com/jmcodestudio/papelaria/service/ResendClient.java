package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.config.ResendProperties;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fala diretamente com a API do Resend. Nunca lança exceção para quem chama —
 * RN-26 exige que falha de e-mail jamais quebre o fluxo de compra, então essa
 * classe absorve o erro e devolve apenas true/false.
 */
@Component
@RequiredArgsConstructor
public class ResendClient {

    private static final Logger log = LoggerFactory.getLogger(ResendClient.class);

    private final ResendProperties propriedades;

    public boolean enviar(String destinatario, String assunto, String html) {
        try {
            Resend client = new Resend(propriedades.apiKey());

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(propriedades.fromEmail())
                    .to(destinatario)
                    .subject(assunto)
                    .html(html)
                    .build();

            client.emails().send(params);
            return true;

        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail via Resend para {}: {}", destinatario, e.getMessage());
            return false;
        }
    }
}

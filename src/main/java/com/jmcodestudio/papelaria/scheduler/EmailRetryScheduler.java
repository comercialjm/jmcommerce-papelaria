package com.jmcodestudio.papelaria.scheduler;

import com.jmcodestudio.papelaria.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** RN-26: verifica a cada 5 minutos se há e-mails que falharam e precisam de retry. */
@Component
@RequiredArgsConstructor
public class EmailRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(EmailRetryScheduler.class);

    private final EmailService emailService;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void tentarReenviosPendentes() {
        try {
            emailService.tentarReenviarConfirmacoesPendentes();
        } catch (Exception e) {
            log.error("Falha ao tentar reenviar e-mails pendentes. Tentando de novo no próximo ciclo.", e);
        }
    }

}

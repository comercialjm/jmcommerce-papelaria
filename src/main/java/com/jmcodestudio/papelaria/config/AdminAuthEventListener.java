package com.jmcodestudio.papelaria.config;

import com.jmcodestudio.papelaria.entity.Administrador;
import com.jmcodestudio.papelaria.repository.AdministradorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** UC-12, RN-29 a RN-32: conta tentativas de login e aplica/limpa o bloqueio. */
@Component
@RequiredArgsConstructor
public class AdminAuthEventListener {

    private final AdministradorRepository administradorRepository;

    @EventListener
    @Transactional
    public void aoFalhar(AbstractAuthenticationFailureEvent evento) {
        String email = evento.getAuthentication().getName();
        administradorRepository.findByEmail(email).ifPresent(Administrador::registrarTentativaFalha);
    }

    @EventListener
    @Transactional
    public void aoTerSucesso(AuthenticationSuccessEvent evento) {
        String email = evento.getAuthentication().getName();
        administradorRepository.findByEmail(email).ifPresent(Administrador::registrarLoginComSucesso);
    }
}

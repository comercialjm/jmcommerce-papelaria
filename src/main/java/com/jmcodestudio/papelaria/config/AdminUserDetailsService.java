package com.jmcodestudio.papelaria.config;

import com.jmcodestudio.papelaria.entity.Administrador;
import com.jmcodestudio.papelaria.repository.AdministradorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UC-12: autentica contra a tabela administrador. estaBloqueado() vira
 * accountLocked para o Spring Security barrar o login automaticamente
 * (UC-12, 4b), sem precisarmos reimplementar essa checagem em outro lugar.
 */
@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final AdministradorRepository administradorRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Administrador admin = administradorRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("E-mail ou senha incorretos."));

        return User.builder()
                .username(admin.getEmail())
                .password(admin.getSenhaHash())
                .authorities("ROLE_ADMIN")
                .accountLocked(admin.estaBloqueado())
                .build();
    }
}

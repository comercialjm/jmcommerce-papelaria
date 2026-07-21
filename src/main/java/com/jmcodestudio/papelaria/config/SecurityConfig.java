package com.jmcodestudio.papelaria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração inicial de segurança.
 *
 * Por enquanto libera todas as rotas públicas para permitir o desenvolvimento
 * do catálogo, carrinho e checkout. A partir do M8 (painel admin), este
 * arquivo será expandido para proteger as rotas /admin/** com login/senha
 * (RN-24, RN-29 do documento de casos de uso v2).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable()); // reativar quando os formulários admin forem implementados

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // RN-29: senhas com BCrypt, custo mínimo 12
        return new BCryptPasswordEncoder(12);
    }

}

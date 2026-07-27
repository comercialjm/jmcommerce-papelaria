package com.jmcodestudio.papelaria.config;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * UC-12: protege as rotas /admin/** com login por formulário (RN-31). O site
 * público continua liberado — só o painel admin exige autenticação.
 *
 * TODO (próximos passos do M8): revisar se algum endpoint de escrita da API
 * pública (carrinho, checkout, frete) precisa de tratamento adicional quando
 * o admin panel também passar a consumir /admin/api/** autenticado.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login", "/css/**", "/js/**").permitAll()
                .requestMatchers("/admin/**", "/admin/api/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .failureUrl("/admin/login?erro")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?saiu")
                .permitAll()
            )
            // RT-03: CSRF protege os formulários do admin. A API pública (carrinho,
            // checkout, frete, webhook) é consumida por JS/Stripe sem sessão
            // autenticada — exigir token ali quebraria o fluxo de compra sem
            // reduzir risco real, já que não há sessão de admin em jogo.
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/webhook/**")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // RN-29: senhas com BCrypt, custo mínimo 12
        return new BCryptPasswordEncoder(12);
    }

    // Sem isso, os eventos de sucesso/falha de login (usados para contar
    // tentativas e bloquear a conta — RN-30) nunca são publicados.
    @Bean
    public AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher publisher) {
        return new DefaultAuthenticationEventPublisher(publisher);
    }

}

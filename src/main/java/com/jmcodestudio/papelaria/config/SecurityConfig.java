package com.jmcodestudio.papelaria.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * UC-12: protege as rotas /admin/** com login por formulário (RN-31). O site
 * público continua liberado — só o painel admin exige autenticação.
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
            // RT-04: cabeçalhos de segurança. CSP aqui é deliberadamente permissivo
            // com 'unsafe-inline' — o sistema usa bastante <script>/style inline nos
            // templates Thymeleaf, e migrar tudo para nonces é um refactor grande
            // demais para o retorno de segurança neste projeto. Ainda assim, a
            // política restringe a origens conhecidas (Cloudinary, Google Fonts,
            // ViaCEP) em vez de liberar tudo.
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline'; " +
                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                    "font-src 'self' https://fonts.gstatic.com; " +
                    "img-src 'self' data: https:; " +
                    "connect-src 'self' https://viacep.com.br; " +
                    "frame-ancestors 'none'"
                ))
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

    // RT-05: precisa rodar ANTES do filtro do Spring Security, para barrar excesso
    // de tentativas sem sequer gastar ciclo verificando senha.
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registro = new FilterRegistrationBean<>(new RateLimitFilter());
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registro;
    }

}

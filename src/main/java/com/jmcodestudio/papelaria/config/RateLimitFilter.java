package com.jmcodestudio.papelaria.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RT-05: rate limiting no login do admin e no webhook do Stripe.
 *
 * Isso é diferente do bloqueio de conta (RN-29/30, por e-mail): aqui o limite é
 * por IP, protegendo contra tentativas distribuídas entre e-mails diferentes ou
 * contra flood no endpoint de webhook. Implementação simples em memória (janela
 * fixa de 60s) — suficiente para o volume desta loja; se o tráfego crescer muito,
 * vale migrar para algo como Bucket4j com backend compartilhado (Redis).
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int LIMITE_LOGIN = 10;    // tentativas de login por IP, por minuto
    private static final int LIMITE_WEBHOOK = 60;   // chamadas de webhook por IP, por minuto
    private static final long JANELA_MS = 60_000;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private record Bucket(AtomicInteger contagem, AtomicLong inicioJanela) {}

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Integer limite = limiteAplicavel(request);

        if (limite != null) {
            String chave = request.getRequestURI() + ":" + obterIp(request);
            if (excedeuLimite(chave, limite)) {
                response.setStatus(429); // Too Many Requests
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Muitas requisições. Tente novamente em instantes.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private Integer limiteAplicavel(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) return null;
        String path = request.getRequestURI();
        if ("/admin/login".equals(path)) return LIMITE_LOGIN;
        if ("/webhook/stripe".equals(path)) return LIMITE_WEBHOOK;
        return null;
    }

    private boolean excedeuLimite(String chave, int limite) {
        long agora = System.currentTimeMillis();
        Bucket bucket = buckets.computeIfAbsent(chave, k -> new Bucket(new AtomicInteger(0), new AtomicLong(agora)));

        synchronized (bucket) {
            if (agora - bucket.inicioJanela().get() > JANELA_MS) {
                bucket.inicioJanela().set(agora);
                bucket.contagem().set(0);
            }
            return bucket.contagem().incrementAndGet() > limite;
        }
    }

    private String obterIp(HttpServletRequest request) {
        String encaminhado = request.getHeader("X-Forwarded-For");
        return (encaminhado != null && !encaminhado.isBlank())
                ? encaminhado.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}

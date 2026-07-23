package com.jmcodestudio.papelaria.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "melhorenvio")
public record MelhorEnvioProperties(
        String baseUrl,
        String token,
        String userAgent
) {}

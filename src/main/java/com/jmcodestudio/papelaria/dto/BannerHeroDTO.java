package com.jmcodestudio.papelaria.dto;

/** UC-01/UC-17: banner hero da home, com fallback quando o admin não configurou nenhum (RN-46). */
public record BannerHeroDTO(
        String imagemUrl,
        String titulo,
        String subtitulo,
        String link
) {}

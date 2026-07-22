package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.dto.BannerHeroDTO;
import com.jmcodestudio.papelaria.entity.ConfiguracaoLoja;
import com.jmcodestudio.papelaria.repository.ConfiguracaoLojaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfiguracaoLojaService {

    private final ConfiguracaoLojaRepository configuracaoLojaRepository;

    /** RN-46: se o admin não configurou um banner, a home usa um texto/imagem padrão. */
    @Transactional(readOnly = true)
    public BannerHeroDTO buscarBannerHero() {
        ConfiguracaoLoja config = configuracaoLojaRepository.findFirstByOrderByIdAsc().orElse(null);

        if (config == null || config.getBannerImagemUrl() == null) {
            return new BannerHeroDTO(
                    null,
                    "Papelaria, com caráter.",
                    "Cadernos, agendas e canetas escolhidos com cuidado para quem gosta de papel de verdade.",
                    "/produtos"
            );
        }

        return new BannerHeroDTO(
                config.getBannerImagemUrl(),
                config.getBannerTitulo(),
                config.getBannerSubtitulo(),
                config.getBannerLink()
        );
    }
}

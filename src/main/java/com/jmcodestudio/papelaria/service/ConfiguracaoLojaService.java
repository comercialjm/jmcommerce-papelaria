package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.dto.BannerHeroDTO;
import com.jmcodestudio.papelaria.dto.ConfiguracaoLojaDTOs.Detalhe;
import com.jmcodestudio.papelaria.dto.ConfiguracaoLojaDTOs.Formulario;
import com.jmcodestudio.papelaria.entity.ConfiguracaoLoja;
import com.jmcodestudio.papelaria.exception.RecursoNaoEncontradoException;
import com.jmcodestudio.papelaria.repository.ConfiguracaoLojaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfiguracaoLojaService {

    private final ConfiguracaoLojaRepository configuracaoLojaRepository;

    /** UC-17: dados atuais para preencher a tela do admin. */
    @Transactional(readOnly = true)
    public Detalhe buscarParaAdmin() {
        ConfiguracaoLoja c = buscarSingleton();
        return new Detalhe(
                c.getNomeLoja(), c.getCepOrigem(), c.getContatoEmail(), c.getContatoWhatsapp(),
                c.getBannerImagemUrl(), c.getBannerTitulo(), c.getBannerSubtitulo(), c.getBannerLink()
        );
    }

    /** UC-17: salva as alterações — RN-44: entram em vigor imediatamente. */
    @Transactional
    public void atualizar(Formulario form) {
        ConfiguracaoLoja c = buscarSingleton();
        c.setNomeLoja(form.nomeLoja());
        c.setCepOrigem(form.cepOrigem());
        c.setContatoEmail(vazioParaNulo(form.contatoEmail()));
        c.setContatoWhatsapp(vazioParaNulo(form.contatoWhatsapp()));
        c.setBannerImagemUrl(vazioParaNulo(form.bannerImagemUrl()));
        c.setBannerTitulo(vazioParaNulo(form.bannerTitulo()));
        c.setBannerSubtitulo(vazioParaNulo(form.bannerSubtitulo()));
        c.setBannerLink(vazioParaNulo(form.bannerLink()));
    }

    private String vazioParaNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }

    private ConfiguracaoLoja buscarSingleton() {
        return configuracaoLojaRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Configuração da loja não encontrada — verifique a migration V1 (linha singleton)."));
    }

    /** RN-27: e-mail de contato exibido nos e-mails transacionais. */
    @Transactional(readOnly = true)
    public String buscarContatoEmail() {
        return configuracaoLojaRepository.findFirstByOrderByIdAsc()
                .map(ConfiguracaoLoja::getContatoEmail)
                .filter(email -> email != null && !email.isBlank())
                .orElse("contato@lojadepapelaria.com.br");
    }

    /** RN-15: CEP de origem configurável (painel admin, a partir do M8). */
    @Transactional(readOnly = true)
    public String buscarCepOrigem() {
        return configuracaoLojaRepository.findFirstByOrderByIdAsc()
                .map(ConfiguracaoLoja::getCepOrigem)
                .orElseThrow(() -> new IllegalStateException(
                        "Configuração da loja não encontrada — verifique a migration V1 (linha singleton)."));
    }

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

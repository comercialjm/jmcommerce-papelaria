package com.jmcodestudio.papelaria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Configurações gerais da loja — linha única (singleton) inserida pela migration V1.
 * O CRUD completo desta tela é escopo do M8 (UC-17); por enquanto só leitura,
 * necessária para a Home exibir o banner hero (RN-46).
 */
@Entity
@Table(name = "configuracao_loja")
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracaoLoja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_loja", nullable = false, length = 150)
    private String nomeLoja;

    @Column(name = "cep_origem", nullable = false, length = 9)
    private String cepOrigem;

    @Column(name = "contato_email", length = 150)
    private String contatoEmail;

    @Column(name = "contato_whatsapp", length = 20)
    private String contatoWhatsapp;

    @Column(name = "banner_imagem_url", length = 500)
    private String bannerImagemUrl;

    @Column(name = "banner_titulo", length = 200)
    private String bannerTitulo;

    @Column(name = "banner_subtitulo", length = 300)
    private String bannerSubtitulo;

    @Column(name = "banner_link", length = 500)
    private String bannerLink;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    @PreUpdate
    protected void aoSalvar() {
        this.atualizadoEm = LocalDateTime.now();
    }
}

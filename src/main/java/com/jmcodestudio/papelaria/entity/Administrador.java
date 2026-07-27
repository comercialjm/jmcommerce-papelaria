package com.jmcodestudio.papelaria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "administrador")
@Getter
@Setter
@NoArgsConstructor
public class Administrador {

    private static final int MAX_TENTATIVAS = 5;
    private static final int MINUTOS_BLOQUEIO = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Column(name = "tentativas_login_falhas", nullable = false)
    private Integer tentativasLoginFalhas = 0;

    @Column(name = "bloqueado_ate")
    private LocalDateTime bloqueadoAte;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void aoPersistir() {
        this.criadoEm = LocalDateTime.now();
    }

    /** UC-12, 4b: 5 tentativas falhas em 15 min bloqueiam o login por 30 min. */
    public boolean estaBloqueado() {
        return bloqueadoAte != null && bloqueadoAte.isAfter(LocalDateTime.now());
    }

    public void registrarTentativaFalha() {
        this.tentativasLoginFalhas++;
        if (this.tentativasLoginFalhas >= MAX_TENTATIVAS) {
            this.bloqueadoAte = LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEIO);
        }
    }

    public void registrarLoginComSucesso() {
        this.tentativasLoginFalhas = 0;
        this.bloqueadoAte = null;
    }
}

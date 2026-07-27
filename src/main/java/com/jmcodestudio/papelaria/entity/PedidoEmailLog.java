package com.jmcodestudio.papelaria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** RN-26: registra cada tentativa de envio, com sucesso/falha, para permitir retry. */
@Entity
@Table(name = "pedido_email_log")
@Getter
@Setter
@NoArgsConstructor
public class PedidoEmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEmail tipo;

    @Column(nullable = false)
    private boolean sucesso;

    @Column(nullable = false)
    private Integer tentativas = 1;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void aoPersistir() {
        this.criadoEm = LocalDateTime.now();
    }
}

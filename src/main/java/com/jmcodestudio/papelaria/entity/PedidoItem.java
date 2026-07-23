package com.jmcodestudio.papelaria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "pedido_item")
@Getter
@Setter
@NoArgsConstructor
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    // Snapshot: mantém o nome mesmo se o produto for renomeado/excluído depois (RN-14 espírito)
    @Column(name = "produto_nome", nullable = false, length = 150)
    private String produtoNome;

    @Column(nullable = false)
    private Integer quantidade;

    // Snapshot: preço no momento da compra, nunca recalculado depois
    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;
}

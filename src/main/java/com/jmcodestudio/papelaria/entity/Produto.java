package com.jmcodestudio.papelaria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "produto")
@Getter
@Setter
@NoArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer estoque = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // RN-16: usados no cálculo de frete (UC-07). Valores padrão se o admin não informar.
    @Column(name = "peso_gramas", nullable = false)
    private Integer pesoGramas = 300;

    @Column(name = "largura_cm", nullable = false, precision = 5, scale = 2)
    private BigDecimal larguraCm = new BigDecimal("20");

    @Column(name = "altura_cm", nullable = false, precision = 5, scale = 2)
    private BigDecimal alturaCm = new BigDecimal("15");

    @Column(name = "comprimento_cm", nullable = false, precision = 5, scale = 2)
    private BigDecimal comprimentoCm = new BigDecimal("5");

    // RN-35: inativo some do catálogo público, mas continua nos pedidos históricos
    @Column(nullable = false)
    private boolean ativo = true;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<ProdutoImagem> imagens = new ArrayList<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void aoPersistir() {
        LocalDateTime agora = LocalDateTime.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    @PreUpdate
    protected void aoAtualizar() {
        this.atualizadoEm = LocalDateTime.now();
    }

    // RN-01: badge "Esgotado" quando estoque = 0
    public boolean isEsgotado() {
        return estoque == null || estoque == 0;
    }

    // RN-02: primeira imagem cadastrada é a capa exibida no grid
    public String getImagemCapa() {
        return imagens.isEmpty() ? null : imagens.get(0).getUrl();
    }

    public void adicionarImagem(ProdutoImagem imagem) {
        if (imagens.size() >= 5) {
            throw new IllegalStateException("Produto já possui o máximo de 5 imagens (RN-03).");
        }
        imagem.setProduto(this);
        imagem.setOrdem(imagens.size());
        imagens.add(imagem);
    }
}

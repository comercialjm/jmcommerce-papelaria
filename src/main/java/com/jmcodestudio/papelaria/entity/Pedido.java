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
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    @Column(name = "cliente_nome", nullable = false, length = 150)
    private String clienteNome;

    @Column(name = "cliente_email", nullable = false, length = 150)
    private String clienteEmail;

    @Column(name = "cliente_telefone", nullable = false, length = 20)
    private String clienteTelefone;

    @Column(name = "endereco_cep", nullable = false, length = 9)
    private String enderecoCep;

    @Column(name = "endereco_rua", nullable = false, length = 200)
    private String enderecoRua;

    @Column(name = "endereco_numero", nullable = false, length = 20)
    private String enderecoNumero;

    @Column(name = "endereco_complemento", length = 100)
    private String enderecoComplemento;

    @Column(name = "endereco_bairro", nullable = false, length = 100)
    private String enderecoBairro;

    @Column(name = "endereco_cidade", nullable = false, length = 100)
    private String enderecoCidade;

    @Column(name = "endereco_uf", nullable = false, length = 2)
    private String enderecoUf;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "frete_valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal freteValor;

    @Column(name = "frete_metodo", length = 50)
    private String freteMetodo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status = StatusPedido.AGUARDANDO_PAGAMENTO;

    @Column(name = "stripe_session_id", length = 200)
    private String stripeSessionId;

    @Column(name = "codigo_rastreio", length = 100)
    private String codigoRastreio;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoItem> itens = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("criadoEm ASC")
    private List<PedidoStatusHistorico> historicoStatus = new ArrayList<>();

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

    public void adicionarItem(PedidoItem item) {
        item.setPedido(this);
        itens.add(item);
    }

    /** RN-43: cada transição de status grava quem/quando/de onde para onde. */
    public void registrarMudancaStatus(StatusPedido novoStatus, String alteradoPor) {
        PedidoStatusHistorico historico = new PedidoStatusHistorico();
        historico.setPedido(this);
        historico.setStatusAnterior(this.status);
        historico.setStatusNovo(novoStatus);
        historico.setAlteradoPor(alteradoPor);
        this.historicoStatus.add(historico);
        this.status = novoStatus;
    }
}

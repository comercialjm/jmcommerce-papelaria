package com.jmcodestudio.papelaria.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class CarrinhoDTOs {

    /** O que o navegador manda: o conteúdo cru do localStorage. */
    public record ItemRequisicao(
            @NotNull Long produtoId,
            @NotNull @Min(1) Integer quantidade
    ) {}

    public record RequisicaoValidacao(
            @NotEmpty @Valid List<ItemRequisicao> itens
    ) {}

    /**
     * O que o servidor devolve para CADA item: dados atuais do produto (RN-14 —
     * nunca confia no snapshot do frontend) e a quantidade já corrigida se
     * necessário (RN-13).
     */
    public record ItemValidado(
            Long produtoId,
            String nome,
            String imagemCapa,
            BigDecimal preco,
            Integer estoque,
            boolean disponivel,       // false = produto removido/desativado/sem estoque (UC-06, 2d)
            Integer quantidade,       // já ajustada ao estoque atual, se necessário
            boolean quantidadeAjustada
    ) {}

    public record RespostaValidacao(
            List<ItemValidado> itens,
            BigDecimal subtotal
    ) {}
}

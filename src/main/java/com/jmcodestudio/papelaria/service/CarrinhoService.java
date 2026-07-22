package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.dto.CarrinhoDTOs.ItemRequisicao;
import com.jmcodestudio.papelaria.dto.CarrinhoDTOs.ItemValidado;
import com.jmcodestudio.papelaria.dto.CarrinhoDTOs.RequisicaoValidacao;
import com.jmcodestudio.papelaria.dto.CarrinhoDTOs.RespostaValidacao;
import com.jmcodestudio.papelaria.entity.Produto;
import com.jmcodestudio.papelaria.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * UC-06: o carrinho em si vive no localStorage do navegador (RN-09). Este serviço
 * existe para revalidar, a cada visita à página do carrinho, os dados que o
 * navegador acha que sabe — preço, estoque e disponibilidade do produto —
 * porque o frontend NUNCA é fonte confiável (RN-10, RN-14).
 */
@Service
@RequiredArgsConstructor
public class CarrinhoService {

    private final ProdutoRepository produtoRepository;

    @Transactional(readOnly = true)
    public RespostaValidacao validar(RequisicaoValidacao requisicao) {
        BigDecimal subtotal = BigDecimal.ZERO;
        List<ItemValidado> itensValidados = new ArrayList<>();

        for (ItemRequisicao itemReq : requisicao.itens()) {
            Produto produto = produtoRepository.findById(itemReq.produtoId()).orElse(null);

            // UC-06, 2d: produto não existe mais, foi desativado ou está sem estoque.
            if (produto == null || !produto.isAtivo() || produto.getEstoque() <= 0) {
                itensValidados.add(new ItemValidado(
                        itemReq.produtoId(), null, null, null, 0, false, 0, false
                ));
                continue;
            }

            // RN-13: quantidade nunca pode superar o estoque atual.
            int quantidadeSolicitada = itemReq.quantidade();
            int quantidadeFinal = Math.min(quantidadeSolicitada, produto.getEstoque());
            boolean foiAjustada = quantidadeFinal != quantidadeSolicitada;

            // RN-14: preço exibido é SEMPRE o atual do banco, nunca o snapshot do cliente.
            BigDecimal subtotalItem = produto.getPreco().multiply(BigDecimal.valueOf(quantidadeFinal));
            subtotal = subtotal.add(subtotalItem);

            itensValidados.add(new ItemValidado(
                    produto.getId(),
                    produto.getNome(),
                    produto.getImagemCapa(),
                    produto.getPreco(),
                    produto.getEstoque(),
                    true,
                    quantidadeFinal,
                    foiAjustada
            ));
        }

        return new RespostaValidacao(itensValidados, subtotal);
    }
}

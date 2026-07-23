package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.dto.FreteDTOs.Opcao;
import com.jmcodestudio.papelaria.dto.FreteDTOs.Requisicao;
import com.jmcodestudio.papelaria.dto.MelhorEnvioDTOs;
import com.jmcodestudio.papelaria.dto.MelhorEnvioDTOs.OpcaoResposta;
import com.jmcodestudio.papelaria.entity.Produto;
import com.jmcodestudio.papelaria.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** UC-07: cálculo de frete por CEP, usando o Melhor Envio como transportadora. */
@Service
@RequiredArgsConstructor
public class FreteService {

    private final ProdutoRepository produtoRepository;
    private final ConfiguracaoLojaService configuracaoLojaService;
    private final MelhorEnvioClient melhorEnvioClient;

    @Transactional(readOnly = true)
    public List<Opcao> calcular(Requisicao requisicao) {
        List<MelhorEnvioDTOs.Produto> produtosParaCotacao = requisicao.itens().stream()
                .map(item -> produtoRepository.findById(item.produtoId())
                        .filter(Produto::isAtivo)
                        .map(produto -> paraProdutoMelhorEnvio(produto, item.quantidade())))
                .flatMap(Optional::stream)
                .toList();

        if (produtosParaCotacao.isEmpty()) {
            return List.of();
        }

        MelhorEnvioDTOs.Requisicao requisicaoExterna = new MelhorEnvioDTOs.Requisicao(
                new MelhorEnvioDTOs.Local(configuracaoLojaService.buscarCepOrigem().replaceAll("\\D", "")),
                new MelhorEnvioDTOs.Local(requisicao.cep()),
                produtosParaCotacao,
                new MelhorEnvioDTOs.Opcoes(false, false)
        );

        List<OpcaoResposta> respostas = melhorEnvioClient.calcular(requisicaoExterna);

        return respostas.stream()
                .filter(r -> r.error() == null) // ignora transportadoras que falharam nesta cotação específica
                .map(this::paraOpcao)
                .toList();
    }

    private MelhorEnvioDTOs.Produto paraProdutoMelhorEnvio(Produto produto, int quantidade) {
        return new MelhorEnvioDTOs.Produto(
                produto.getId().toString(),
                Math.max(1, produto.getLarguraCm().intValue()),
                Math.max(1, produto.getAlturaCm().intValue()),
                Math.max(1, produto.getComprimentoCm().intValue()),
                BigDecimal.valueOf(produto.getPesoGramas()).divide(BigDecimal.valueOf(1000)), // g -> kg
                produto.getPreco(),
                quantidade
        );
    }

    private Opcao paraOpcao(OpcaoResposta r) {
        BigDecimal preco = r.customPrice() != null ? r.customPrice() : r.price();
        Integer prazo = r.customDeliveryTime() != null ? r.customDeliveryTime() : r.deliveryTime();
        String transportadora = r.company() != null ? r.company().name() : "";
        return new Opcao(r.name(), transportadora, preco, prazo);
    }
}

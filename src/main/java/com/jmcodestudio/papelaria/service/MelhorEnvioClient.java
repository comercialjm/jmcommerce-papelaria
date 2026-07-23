package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.config.MelhorEnvioProperties;
import com.jmcodestudio.papelaria.dto.MelhorEnvioDTOs.OpcaoResposta;
import com.jmcodestudio.papelaria.dto.MelhorEnvioDTOs.Requisicao;
import com.jmcodestudio.papelaria.exception.ServicoExternoIndisponivelException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Fala diretamente com a API do Melhor Envio (sandbox ou produção, conforme
 * melhorenvio.base-url). Nenhuma regra de negócio aqui — só a chamada HTTP.
 */
@Component
@RequiredArgsConstructor
public class MelhorEnvioClient {

    private final MelhorEnvioProperties propriedades;

    public List<OpcaoResposta> calcular(Requisicao requisicao) {
        RestClient client = RestClient.create(propriedades.baseUrl());

        try {
            OpcaoResposta[] resposta = client.post()
                    .uri("/api/v2/me/shipment/calculate")
                    .header("Authorization", "Bearer " + propriedades.token())
                    .header("User-Agent", propriedades.userAgent())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(requisicao)
                    .retrieve()
                    .body(OpcaoResposta[].class);

            return resposta == null ? List.of() : List.of(resposta);

        } catch (RestClientException e) {
            throw new ServicoExternoIndisponivelException(
                    "Não foi possível calcular o frete no momento. Tente novamente em alguns instantes.", e);
        }
    }
}

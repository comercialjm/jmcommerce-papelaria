package com.jmcodestudio.papelaria.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/** Formato exigido pela API do Melhor Envio (POST /api/v2/me/shipment/calculate). */
public class MelhorEnvioDTOs {

    public record Local(@JsonProperty("postal_code") String postalCode) {}

    public record Produto(
            String id,
            Integer width,
            Integer height,
            Integer length,
            BigDecimal weight,
            @JsonProperty("insurance_value") BigDecimal insuranceValue,
            Integer quantity
    ) {}

    public record Opcoes(boolean receipt, @JsonProperty("own_hand") boolean ownHand) {}

    public record Requisicao(
            Local from,
            Local to,
            List<Produto> products,
            Opcoes options
    ) {}

    /** Um item da resposta — uma opção de envio (ex: PAC, SEDEX) de uma transportadora. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OpcaoResposta(
            Long id,
            String name,
            BigDecimal price,
            @JsonProperty("custom_price") BigDecimal customPrice,
            @JsonProperty("delivery_time") Integer deliveryTime,
            @JsonProperty("custom_delivery_time") Integer customDeliveryTime,
            Empresa company,
            String error // presente quando esta transportadora específica falhou nesta cotação
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Empresa(String name) {}
}

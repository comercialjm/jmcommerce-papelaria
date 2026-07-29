package com.jmcodestudio.papelaria.controller.api;

import com.jmcodestudio.papelaria.exception.RegraDeNegocioException;
import com.jmcodestudio.papelaria.service.ConfiguracaoLojaService;
import com.jmcodestudio.papelaria.service.PedidoService;
import com.jmcodestudio.papelaria.service.StripeCheckoutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CheckoutApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class CheckoutApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    @MockBean
    private StripeCheckoutService stripeCheckoutService;

    // Necessário porque ConfiguracaoLojaModelAdvice (@ControllerAdvice) é carregado
    // em QUALQUER @WebMvcTest, não só onde ele se aplica de fato em produção.
    @MockBean
    private ConfiguracaoLojaService configuracaoLojaService;

    @Test
    void finalizar_deveRetornar400_quandoCamposObrigatoriosFaltando() throws Exception {
        String payloadIncompleto = """
                {
                    "nomeCompleto": "",
                    "email": "nao-e-email",
                    "itens": []
                }
                """;

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadIncompleto))
                .andExpect(status().isBadRequest());
    }

    @Test
    void finalizar_deveRetornar409_quandoEstoqueInsuficiente() throws Exception {
        when(pedidoService.criarAguardandoPagamento(any()))
                .thenThrow(new RegraDeNegocioException("O produto \"Caderno\" não possui mais estoque suficiente."));

        String payloadValido = """
                {
                    "nomeCompleto": "Cliente Teste",
                    "email": "cliente@teste.com",
                    "telefone": "(21) 99999-9999",
                    "cep": "28621350",
                    "rua": "Rua Teste",
                    "numero": "123",
                    "bairro": "Bairro",
                    "cidade": "Cidade",
                    "uf": "RJ",
                    "itens": [{"produtoId": 1, "quantidade": 5}],
                    "frete": {"servico": "PAC", "transportadora": "Correios", "preco": 20.00, "prazoDias": 5}
                }
                """;

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadValido))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("estoque suficiente")));
    }

}

package com.jmcodestudio.papelaria.controller.api;

import com.jmcodestudio.papelaria.config.StripeProperties;
import com.jmcodestudio.papelaria.service.ConfiguracaoLojaService;
import com.jmcodestudio.papelaria.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RN-23: o webhook precisa rejeitar qualquer payload com assinatura inválida.
 * addFilters = false porque este teste é sobre a lógica do controller (verificação
 * de assinatura do Stripe), não sobre a cadeia de segurança em si.
 */
@WebMvcTest(controllers = StripeWebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
class StripeWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StripeProperties stripeProperties;

    @MockBean
    private PedidoService pedidoService;

    // Necessário porque ConfiguracaoLojaModelAdvice (@ControllerAdvice) é carregado
    // em QUALQUER @WebMvcTest, não só onde ele se aplica de fato em produção.
    @MockBean
    private ConfiguracaoLojaService configuracaoLojaService;

    @Test
    void receber_deveRetornar400_quandoAssinaturaInvalida() throws Exception {
        org.mockito.Mockito.when(stripeProperties.webhookSecret()).thenReturn("whsec_segredo_de_teste");

        mockMvc.perform(post("/webhook/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "t=123,v1=assinatura_forjada_invalida")
                        .content("{\"type\":\"checkout.session.completed\"}"))
                .andExpect(status().isBadRequest());

        // Assinatura inválida precisa barrar ANTES de qualquer tentativa de
        // confirmar pagamento — nunca deve chegar a chamar o serviço de pedido.
        verifyNoInteractions(pedidoService);
    }

}

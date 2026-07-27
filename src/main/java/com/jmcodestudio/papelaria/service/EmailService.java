package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.entity.*;
import com.jmcodestudio.papelaria.repository.PedidoEmailLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * UC-E1/E2/E3: monta e envia os e-mails transacionais via Resend.
 *
 * RN-26: falha no envio NUNCA impede a conclusão da compra — todo método aqui
 * captura seus próprios erros e apenas registra o resultado em pedido_email_log,
 * nunca lança exceção para quem chamou.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ResendClient resendClient;
    private final PedidoEmailLogRepository emailLogRepository;
    private final ConfiguracaoLojaService configuracaoLojaService;

    /** UC-E1: disparado pelo webhook do Stripe após confirmação de pagamento. */
    @Transactional
    public void enviarConfirmacaoPagamento(Pedido pedido) {
        String assunto = "Pagamento confirmado — pedido " + pedido.getNumero();
        String html = montarHtmlConfirmacao(pedido);

        boolean sucesso = resendClient.enviar(pedido.getClienteEmail(), assunto, html);
        registrarLog(pedido, TipoEmail.CONFIRMACAO, sucesso, 1);

        if (!sucesso) {
            log.warn("E-mail de confirmação do pedido {} falhou na 1ª tentativa. Retry em ~5min.",
                    pedido.getNumero());
        }
    }

    /**
     * RN-26: retry automático 1x após 5 minutos. Chamado periodicamente pelo
     * EmailRetryScheduler. Hoje cobre apenas UC-E1 (o único gatilho já existente
     * no sistema — UC-E2/E3 disparam de ações do admin, que chegam no M8).
     */
    @Transactional
    public void tentarReenviarConfirmacoesPendentes() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(5);
        List<PedidoEmailLog> pendentes = emailLogRepository
                .findByTipoAndSucessoFalseAndTentativasAndCriadoEmBefore(TipoEmail.CONFIRMACAO, 1, limite);

        for (PedidoEmailLog logAntigo : pendentes) {
            Pedido pedido = logAntigo.getPedido();
            String assunto = "Pagamento confirmado — pedido " + pedido.getNumero();
            String html = montarHtmlConfirmacao(pedido);

            boolean sucesso = resendClient.enviar(pedido.getClienteEmail(), assunto, html);
            logAntigo.setSucesso(sucesso);
            logAntigo.setTentativas(2);

            if (!sucesso) {
                log.warn("2ª tentativa de e-mail para o pedido {} também falhou. Registrado como não entregue.",
                        pedido.getNumero());
            }
        }
    }

    /** UC-E2: disparado quando o admin marca o pedido como ENVIADO. */
    @Transactional
    public void enviarPedidoEnviado(Pedido pedido) {
        String assunto = "Seu pedido foi enviado — " + pedido.getNumero();
        String html = montarHtmlEnviado(pedido);
        boolean sucesso = resendClient.enviar(pedido.getClienteEmail(), assunto, html);
        registrarLog(pedido, TipoEmail.ENVIADO, sucesso, 1);
    }

    /**
     * UC-E3. RN-28: só é chamado quando o pedido já estava PAGO — pedidos
     * expirados de AGUARDANDO_PAGAMENTO não geram e-mail (quem decide isso é
     * quem chama este método, não esta classe).
     */
    @Transactional
    public void enviarPedidoCancelado(Pedido pedido, String motivo) {
        String assunto = "Pedido cancelado — " + pedido.getNumero();
        String html = montarHtmlCancelado(pedido, motivo);
        boolean sucesso = resendClient.enviar(pedido.getClienteEmail(), assunto, html);
        registrarLog(pedido, TipoEmail.CANCELADO, sucesso, 1);
    }

    private String montarHtmlEnviado(Pedido pedido) {
        String contato = configuracaoLojaService.buscarContatoEmail();
        String rastreio = (pedido.getCodigoRastreio() != null && !pedido.getCodigoRastreio().isBlank())
                ? "Código de rastreio: <strong>" + pedido.getCodigoRastreio() + "</strong>"
                : "Em breve enviaremos o código de rastreio.";

        return """
                <div style="font-family: Georgia, serif; max-width: 480px; margin: 0 auto; color: #2B2620;">
                    <h1 style="font-size: 20px;">Loja de Papelaria</h1>
                    <p>Seu pedido foi enviado!</p>
                    <p style="font-family: monospace; font-size: 16px; color: #2F4A3E;">%s</p>
                    <p>%s</p>
                    <p style="color:#6B6255; font-size:13px;">Endereço de entrega: %s, %s%s - %s, %s/%s - CEP %s</p>
                    <p style="color:#6B6255; font-size:12px; margin-top: 24px;">
                        Dúvidas? Fale com a gente em <a href="mailto:%s">%s</a>.
                    </p>
                </div>
                """.formatted(
                pedido.getNumero(), rastreio,
                pedido.getEnderecoRua(), pedido.getEnderecoNumero(),
                pedido.getEnderecoComplemento() != null ? " (" + pedido.getEnderecoComplemento() + ")" : "",
                pedido.getEnderecoBairro(), pedido.getEnderecoCidade(), pedido.getEnderecoUf(), pedido.getEnderecoCep(),
                contato, contato
        );
    }

    private String montarHtmlCancelado(Pedido pedido, String motivo) {
        String contato = configuracaoLojaService.buscarContatoEmail();
        String linhaMotivo = (motivo != null && !motivo.isBlank())
                ? "<p><strong>Motivo:</strong> " + motivo + "</p>" : "";

        return """
                <div style="font-family: Georgia, serif; max-width: 480px; margin: 0 auto; color: #2B2620;">
                    <h1 style="font-size: 20px;">Loja de Papelaria</h1>
                    <p>Seu pedido foi cancelado.</p>
                    <p style="font-family: monospace; font-size: 16px; color: #2F4A3E;">%s</p>
                    %s
                    <p>O reembolso do valor pago já foi solicitado e deve aparecer na sua fatura em alguns dias úteis.</p>
                    <p style="color:#6B6255; font-size:12px; margin-top: 24px;">
                        Dúvidas? Fale com a gente em <a href="mailto:%s">%s</a>.
                    </p>
                </div>
                """.formatted(pedido.getNumero(), linhaMotivo, contato, contato);
    }

    private void registrarLog(Pedido pedido, TipoEmail tipo, boolean sucesso, int tentativas) {
        PedidoEmailLog registro = new PedidoEmailLog();
        registro.setPedido(pedido);
        registro.setTipo(tipo);
        registro.setSucesso(sucesso);
        registro.setTentativas(tentativas);
        emailLogRepository.save(registro);
    }

    private String montarHtmlConfirmacao(Pedido pedido) {
        StringBuilder itensHtml = new StringBuilder();
        for (PedidoItem item : pedido.getItens()) {
            BigDecimal subtotalItem = item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
            itensHtml.append("""
                    <tr>
                        <td style="padding:8px 0; border-bottom:1px solid #E4DFD3;">%d&times; %s</td>
                        <td style="padding:8px 0; border-bottom:1px solid #E4DFD3; text-align:right;">R$ %s</td>
                    </tr>
                    """.formatted(item.getQuantidade(), item.getProdutoNome(), formatarReal(subtotalItem)));
        }

        String contato = configuracaoLojaService.buscarContatoEmail();

        return """
                <div style="font-family: Georgia, serif; max-width: 480px; margin: 0 auto; color: #2B2620;">
                    <h1 style="font-size: 20px;">Loja de Papelaria</h1>
                    <p>Recebemos seu pagamento! Estamos preparando seu pedido.</p>
                    <p style="font-family: monospace; font-size: 16px; color: #2F4A3E;">%s</p>
                    <table style="width:100%%; border-collapse: collapse; margin: 16px 0;">
                        %s
                    </table>
                    <p><strong>Total pago:</strong> R$ %s</p>
                    <p style="color:#6B6255; font-size:13px;">
                        Endereço de entrega: %s, %s%s - %s, %s/%s - CEP %s
                    </p>
                    <p style="color:#6B6255; font-size:12px; margin-top: 24px;">
                        Dúvidas? Fale com a gente em <a href="mailto:%s">%s</a>.
                    </p>
                </div>
                """.formatted(
                pedido.getNumero(),
                itensHtml,
                formatarReal(pedido.getTotal()),
                pedido.getEnderecoRua(), pedido.getEnderecoNumero(),
                pedido.getEnderecoComplemento() != null ? " (" + pedido.getEnderecoComplemento() + ")" : "",
                pedido.getEnderecoBairro(), pedido.getEnderecoCidade(), pedido.getEnderecoUf(), pedido.getEnderecoCep(),
                contato, contato
        );
    }

    private String formatarReal(BigDecimal valor) {
        return String.format(new java.util.Locale("pt", "BR"), "%,.2f", valor);
    }
}

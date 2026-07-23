package com.jmcodestudio.papelaria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** UC-07, 4a: API de frete indisponível — nunca deixamos isso quebrar o carrinho. */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServicoExternoIndisponivelException extends RuntimeException {
    public ServicoExternoIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

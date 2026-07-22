package com.jmcodestudio.papelaria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @ResponseStatus garante que, quando esta exceção escapa de um controller de
 * página pública (não interceptado pelo GlobalExceptionHandler, restrito à API),
 * o Spring devolve HTTP 404 e resolve templates/error/404.html normalmente.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}

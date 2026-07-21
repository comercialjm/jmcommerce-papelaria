package com.jmcodestudio.papelaria.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpoErro(ex.getMessage()));
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<Map<String, Object>> handleRegraDeNegocio(RegraDeNegocioException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(corpoErro(ex.getMessage()));
    }

    /**
     * Rede de segurança: se alguma regra de negócio no service deixar passar uma
     * exclusão que o banco rejeita por chave estrangeira (ex: categoria com produto
     * inativo vinculado — caso não coberto literalmente pela RN-36, que fala só de
     * produtos ativos), devolve 409 com mensagem clara em vez de 500 genérico.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegridade(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(corpoErro("Este registro não pode ser removido porque outros dados dependem dele."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> camposComErro = new HashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            camposComErro.put(erro.getField(), erro.getDefaultMessage());
        }
        Map<String, Object> corpo = corpoErro("Há campos inválidos no formulário.");
        corpo.put("campos", camposComErro);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }

    private Map<String, Object> corpoErro(String mensagem) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("timestamp", LocalDateTime.now());
        corpo.put("mensagem", mensagem);
        return corpo;
    }
}

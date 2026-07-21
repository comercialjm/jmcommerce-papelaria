package com.jmcodestudio.papelaria.exception;

/**
 * Lançada quando uma ação viola uma regra de negócio documentada em use-cases-v2.md
 * (ex: RN-26 excluir produto com pedidos, RN-36 excluir categoria com produtos ativos,
 * RN-37 nome de categoria duplicado no mesmo nível).
 */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}

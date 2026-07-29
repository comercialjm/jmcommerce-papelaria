package com.jmcodestudio.papelaria.controller;

import com.jmcodestudio.papelaria.dto.ConfiguracaoLojaDTOs.Detalhe;
import com.jmcodestudio.papelaria.service.ConfiguracaoLojaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Disponibiliza os dados de configuração da loja (nome, contato) para o
 * header.html e footer.html em todas as páginas públicas — sem isso, esses
 * fragmentos mostravam "Loja de Papelaria" fixo no texto, ignorando o que o
 * admin configura em UC-17, por mais que ele mudasse o nome lá.
 *
 * Escopo restrito via assignableTypes (não basePackages) para não disparar
 * essa consulta à toa em toda chamada de API REST ou página do admin, que não
 * renderizam esses fragmentos.
 */
@ControllerAdvice(assignableTypes = {
        HomeController.class,
        CatalogoController.class,
        ProdutoPublicoController.class,
        CarrinhoController.class,
        CheckoutController.class,
        PedidoPublicoController.class
})
@RequiredArgsConstructor
public class ConfiguracaoLojaModelAdvice {

    private final ConfiguracaoLojaService configuracaoLojaService;

    @ModelAttribute("configuracaoLoja")
    public Detalhe configuracaoLoja() {
        return configuracaoLojaService.buscarParaAdmin();
    }
}

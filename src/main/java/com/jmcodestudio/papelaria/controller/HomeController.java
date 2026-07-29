package com.jmcodestudio.papelaria.controller;

import com.jmcodestudio.papelaria.service.CategoriaService;
import com.jmcodestudio.papelaria.service.ConfiguracaoLojaService;
import com.jmcodestudio.papelaria.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private static final int QUANTIDADE_DESTAQUES = 8;
    private static final int QUANTIDADE_CATEGORIAS_HOME = 3; // a 4ª "vaga" do grid vira o card "Mais"

    private final CategoriaService categoriaService;
    private final ProdutoService produtoService;
    private final ConfiguracaoLojaService configuracaoLojaService;

    @GetMapping("/")
    public String home(Model model) {
        var todasCategorias = categoriaService.listarParaCatalogoPublico();

        model.addAttribute("banner", configuracaoLojaService.buscarBannerHero());
        model.addAttribute("categorias", todasCategorias.stream().limit(QUANTIDADE_CATEGORIAS_HOME).toList());
        model.addAttribute("temMaisCategorias", todasCategorias.size() > QUANTIDADE_CATEGORIAS_HOME);
        model.addAttribute("produtosDestaque", produtoService.listarDestaque(QUANTIDADE_DESTAQUES));
        return "home";
    }

}

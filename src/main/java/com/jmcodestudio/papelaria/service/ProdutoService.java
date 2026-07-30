package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.dto.ProdutoDTOs;
import com.jmcodestudio.papelaria.dto.ProdutoDTOs.Detalhe;
import com.jmcodestudio.papelaria.dto.ProdutoDTOs.Formulario;
import com.jmcodestudio.papelaria.dto.ProdutoDTOs.Resumo;
import com.jmcodestudio.papelaria.entity.Produto;
import com.jmcodestudio.papelaria.entity.ProdutoImagem;
import com.jmcodestudio.papelaria.exception.RecursoNaoEncontradoException;
import com.jmcodestudio.papelaria.repository.CategoriaRepository;
import com.jmcodestudio.papelaria.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaService categoriaService;
    private final CategoriaRepository categoriaRepository;

    /** UC-02: catálogo público, só produtos ativos. RN-38: filtrar por uma
     * categoria de nível 1 também traz produtos das suas subcategorias, já que
     * a navegação pública trata a hierarquia como transparente. */
    @Transactional(readOnly = true)
    public Page<Resumo> listarCatalogo(Long categoriaId, Pageable pageable) {
        Page<Produto> pagina;

        if (categoriaId != null) {
            List<Long> idsCategorias = new ArrayList<>();
            idsCategorias.add(categoriaId);
            categoriaRepository.findByParentId(categoriaId).forEach(sub -> idsCategorias.add(sub.getId()));
            pagina = produtoRepository.findByAtivoTrueAndCategoriaIdIn(idsCategorias, pageable);
        } else {
            pagina = produtoRepository.findByAtivoTrue(pageable);
        }

        return pagina.map(this::paraResumo);
    }

    /** UC-01: seção "Novidades" da home — produtos ativos mais recentes. */
    @Transactional(readOnly = true)
    public List<Resumo> listarDestaque(int quantidade) {
        return produtoRepository
                .findByAtivoTrueOrderByCriadoEmDesc(PageRequest.of(0, quantidade))
                .map(this::paraResumo)
                .getContent();
    }

    /** UC-04: busca simples por nome/descrição (RN-06/RN-07/RN-08 refinados no M4). */
    @Transactional(readOnly = true)
    public Page<Resumo> buscar(String termo, Pageable pageable) {
        return produtoRepository.buscarPorTermo(termo, pageable).map(this::paraResumo);
    }

    /** UC-03: detalhe público. Produto inativo não deve ser acessível (404). */
    @Transactional(readOnly = true)
    public Detalhe buscarDetalhePublico(Long id) {
        Produto produto = buscarEntidadeOuFalhar(id);
        if (!produto.isAtivo()) {
            throw new RecursoNaoEncontradoException("Produto não encontrado: id " + id);
        }
        return paraDetalhe(produto);
    }

    /** UC-14a/UC-14c: admin enxerga produtos ativos e inativos, com mais detalhe. */
    @Transactional(readOnly = true)
    public Page<ProdutoDTOs.ResumoAdmin> listarParaAdmin(String nomeFiltro, Pageable pageable) {
        String filtro = (nomeFiltro == null) ? "" : nomeFiltro;
        return produtoRepository.findByNomeContainingIgnoreCase(filtro, pageable).map(this::paraResumoAdmin);
    }

    @Transactional(readOnly = true)
    public Detalhe buscarDetalheAdmin(Long id) {
        return paraDetalhe(buscarEntidadeOuFalhar(id));
    }

    /** UC-14b: cadastro de novo produto. */
    @Transactional
    public Detalhe criar(Formulario form) {
        Produto produto = new Produto();
        aplicarFormulario(produto, form);
        return paraDetalhe(produtoRepository.save(produto));
    }

    /** UC-14c: edição de produto existente. */
    @Transactional
    public Detalhe atualizar(Long id, Formulario form) {
        Produto produto = buscarEntidadeOuFalhar(id);
        aplicarFormulario(produto, form);
        return paraDetalhe(produto);
    }

    /**
     * UC-14d. RN-26/RN-34: nunca há exclusão de produto, apenas ativação/desativação —
     * o produto permanece associado aos pedidos históricos (RN-35).
     */
    @Transactional
    public void alterarStatus(Long id, boolean ativo) {
        Produto produto = buscarEntidadeOuFalhar(id);
        produto.setAtivo(ativo);
    }

    private void aplicarFormulario(Produto produto, Formulario form) {
        produto.setNome(form.nome());
        produto.setDescricao(form.descricao());
        produto.setPreco(form.preco());
        produto.setEstoque(form.estoque());
        produto.setCategoria(categoriaService.buscarEntidadeOuFalhar(form.categoriaId()));

        // RN-16: valores padrão quando o admin não informa peso/dimensões.
        produto.setPesoGramas(form.pesoGramas() != null ? form.pesoGramas() : 300);
        produto.setLarguraCm(form.larguraCm() != null ? form.larguraCm() : new BigDecimal("20"));
        produto.setAlturaCm(form.alturaCm() != null ? form.alturaCm() : new BigDecimal("15"));
        produto.setComprimentoCm(form.comprimentoCm() != null ? form.comprimentoCm() : new BigDecimal("5"));

        // RN-03: substitui a lista de imagens pela nova ordem enviada pelo admin.
        // O upload físico do arquivo (Cloudinary/filesystem) é resolvido antes de
        // chegar aqui — este serviço só persiste as URLs já hospedadas.
        produto.getImagens().clear();
        for (String url : form.imagens()) {
            ProdutoImagem imagem = new ProdutoImagem();
            imagem.setUrl(url);
            produto.adicionarImagem(imagem);
        }
    }

    private Produto buscarEntidadeOuFalhar(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: id " + id));
    }

    private Resumo paraResumo(Produto p) {
        boolean novo = p.getCriadoEm() != null
                && p.getCriadoEm().isAfter(LocalDateTime.now().minusDays(30));
        return new Resumo(p.getId(), p.getNome(), p.getPreco(), p.getImagemCapa(), p.isEsgotado(), novo);
    }

    private ProdutoDTOs.ResumoAdmin paraResumoAdmin(Produto p) {
        return new ProdutoDTOs.ResumoAdmin(
                p.getId(), p.getNome(), p.getCategoria().getNome(),
                p.getPreco(), p.getEstoque(), p.isAtivo(), p.getImagemCapa()
        );
    }

    private Detalhe paraDetalhe(Produto p) {
        return new Detalhe(
                p.getId(), p.getNome(), p.getDescricao(), p.getPreco(), p.getEstoque(), p.isEsgotado(),
                p.getCategoria().getId(), p.getCategoria().getNome(),
                p.getPesoGramas(), p.getLarguraCm(), p.getAlturaCm(), p.getComprimentoCm(),
                p.isAtivo(), p.getImagens().stream().map(ProdutoImagem::getUrl).toList()
        );
    }
}

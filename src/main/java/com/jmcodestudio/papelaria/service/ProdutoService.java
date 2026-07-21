package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.dto.ProdutoDTOs.Detalhe;
import com.jmcodestudio.papelaria.dto.ProdutoDTOs.Formulario;
import com.jmcodestudio.papelaria.dto.ProdutoDTOs.Resumo;
import com.jmcodestudio.papelaria.entity.Produto;
import com.jmcodestudio.papelaria.entity.ProdutoImagem;
import com.jmcodestudio.papelaria.exception.RecursoNaoEncontradoException;
import com.jmcodestudio.papelaria.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaService categoriaService;

    /** UC-02: catálogo público, só produtos ativos. */
    @Transactional(readOnly = true)
    public Page<Resumo> listarCatalogo(Long categoriaId, Pageable pageable) {
        Page<Produto> pagina = (categoriaId != null)
                ? produtoRepository.findByAtivoTrueAndCategoriaId(categoriaId, pageable)
                : produtoRepository.findByAtivoTrue(pageable);
        return pagina.map(this::paraResumo);
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

    /** UC-14a/UC-14c: admin enxerga produtos ativos e inativos. */
    @Transactional(readOnly = true)
    public Page<Resumo> listarParaAdmin(String nomeFiltro, Pageable pageable) {
        String filtro = (nomeFiltro == null) ? "" : nomeFiltro;
        return produtoRepository.findByNomeContainingIgnoreCase(filtro, pageable).map(this::paraResumo);
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
        produto.setPesoGramas(form.pesoGramas());
        produto.setLarguraCm(form.larguraCm());
        produto.setAlturaCm(form.alturaCm());
        produto.setComprimentoCm(form.comprimentoCm());

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
        return new Resumo(p.getId(), p.getNome(), p.getPreco(), p.getImagemCapa(), p.isEsgotado());
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

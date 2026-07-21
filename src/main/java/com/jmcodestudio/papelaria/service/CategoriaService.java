package com.jmcodestudio.papelaria.service;

import com.jmcodestudio.papelaria.dto.CategoriaDTOs.Formulario;
import com.jmcodestudio.papelaria.dto.CategoriaDTOs.Resposta;
import com.jmcodestudio.papelaria.entity.Categoria;
import com.jmcodestudio.papelaria.exception.RecursoNaoEncontradoException;
import com.jmcodestudio.papelaria.exception.RegraDeNegocioException;
import com.jmcodestudio.papelaria.repository.CategoriaRepository;
import com.jmcodestudio.papelaria.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    /** RN-38: catálogo público exibe só categorias de nível 1, ativas. */
    @Transactional(readOnly = true)
    public List<Resposta> listarParaCatalogoPublico() {
        return categoriaRepository.findByParentIsNullAndAtivaTrueOrderByNome()
                .stream().map(this::paraRespostaSimples).toList();
    }

    /** Admin (UC-15): mostra a árvore completa, incluindo inativas e subcategorias. */
    @Transactional(readOnly = true)
    public List<Resposta> listarParaAdmin() {
        return categoriaRepository.findAll().stream()
                .filter(c -> !c.isSubcategoria())
                .map(this::paraRespostaComSubcategorias)
                .toList();
    }

    @Transactional(readOnly = true)
    public Categoria buscarEntidadeOuFalhar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada: id " + id));
    }

    @Transactional
    public Resposta criar(Formulario form) {
        validarNomeUnicoNoNivel(form.nome(), form.parentId(), null);

        Categoria categoria = new Categoria();
        categoria.setNome(form.nome());
        categoria.setImagemUrl(form.imagemUrl());

        if (form.parentId() != null) {
            categoria.setParent(buscarEntidadeOuFalhar(form.parentId()));
        }

        return paraRespostaSimples(categoriaRepository.save(categoria));
    }

    @Transactional
    public Resposta atualizar(Long id, Formulario form) {
        Categoria categoria = buscarEntidadeOuFalhar(id);
        validarNomeUnicoNoNivel(form.nome(), form.parentId(), id);

        categoria.setNome(form.nome());
        categoria.setImagemUrl(form.imagemUrl());
        categoria.setParent(form.parentId() != null ? buscarEntidadeOuFalhar(form.parentId()) : null);

        return paraRespostaSimples(categoria);
    }

    /**
     * RN-39: ao desativar uma categoria pai, todas as subcategorias são
     * desativadas automaticamente em cascata.
     */
    @Transactional
    public void alterarStatus(Long id, boolean ativa) {
        Categoria categoria = buscarEntidadeOuFalhar(id);
        categoria.setAtiva(ativa);

        if (!ativa) {
            desativarSubcategoriasEmCascata(categoria);
        }
    }

    private void desativarSubcategoriasEmCascata(Categoria categoria) {
        for (Categoria sub : categoria.getSubcategorias()) {
            sub.setAtiva(false);
            desativarSubcategoriasEmCascata(sub);
        }
    }

    /** RN-36: bloqueia exclusão se houver produtos ativos vinculados ou subcategorias. */
    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarEntidadeOuFalhar(id);

        if (produtoRepository.existsByCategoriaIdAndAtivoTrue(id)) {
            throw new RegraDeNegocioException(
                    "Não é possível excluir a categoria: há produtos ativos vinculados a ela (RN-36). Desative-a em vez disso.");
        }
        if (!categoria.getSubcategorias().isEmpty()) {
            throw new RegraDeNegocioException(
                    "Não é possível excluir uma categoria que possui subcategorias. Remova ou realoque-as primeiro.");
        }

        categoriaRepository.delete(categoria);
    }

    /** RN-37: nome único dentro do mesmo nível (mesmo parent). */
    private void validarNomeUnicoNoNivel(String nome, Long parentId, Long excluindoId) {
        categoriaRepository.findConflitoDeNomeNoMesmoNivel(nome, parentId, excluindoId)
                .ifPresent(c -> {
                    throw new RegraDeNegocioException(
                            "Já existe uma categoria chamada \"" + nome + "\" neste nível (RN-37).");
                });
    }

    private Resposta paraRespostaSimples(Categoria c) {
        long qtdProdutos = produtoRepository.countByCategoriaIdAndAtivoTrue(c.getId());
        return new Resposta(
                c.getId(), c.getNome(), c.getImagemUrl(),
                c.getParent() != null ? c.getParent().getId() : null,
                c.getParent() != null ? c.getParent().getNome() : null,
                c.isAtiva(), qtdProdutos, List.of()
        );
    }

    private Resposta paraRespostaComSubcategorias(Categoria c) {
        long qtdProdutos = produtoRepository.countByCategoriaIdAndAtivoTrue(c.getId());
        List<Resposta> subs = c.getSubcategorias().stream().map(this::paraRespostaSimples).toList();
        return new Resposta(
                c.getId(), c.getNome(), c.getImagemUrl(),
                null, null, c.isAtiva(), qtdProdutos, subs
        );
    }
}

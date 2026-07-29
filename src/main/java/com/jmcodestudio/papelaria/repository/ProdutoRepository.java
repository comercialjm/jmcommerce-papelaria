package com.jmcodestudio.papelaria.repository;

import com.jmcodestudio.papelaria.entity.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Catálogo público (UC-02): apenas produtos ativos
    Page<Produto> findByAtivoTrue(Pageable pageable);

    Page<Produto> findByAtivoTrueAndCategoriaId(Long categoriaId, Pageable pageable);

    // Home (UC-01): produtos em destaque = mais recentes ativos
    Page<Produto> findByAtivoTrueOrderByCriadoEmDesc(Pageable pageable);

    // Busca simples (UC-04). RN-07 exige ignorar acentos também — isso depende da
    // extensão "unaccent" do Postgres e será refinado no M4 junto com a tela de busca.
    @Query("""
        SELECT p FROM Produto p
        WHERE p.ativo = true
        AND (LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%'))
             OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :termo, '%')))
        """)
    Page<Produto> buscarPorTermo(@Param("termo") String termo, Pageable pageable);

    // Listagem do admin (UC-14a): inclui produtos inativos. EntityGraph evita N+1 —
    // sem isso, o Hibernate disparava uma query extra por linha só para
    // categoria.getNome() (usado no ResumoAdmin).
    @EntityGraph(attributePaths = "categoria")
    Page<Produto> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    long countByCategoriaIdAndAtivoTrue(Long categoriaId);

    boolean existsByCategoriaIdAndAtivoTrue(Long categoriaId);

    // UC-13: estatísticas do dashboard
    long countByAtivoTrueAndEstoque(Integer estoque);
}

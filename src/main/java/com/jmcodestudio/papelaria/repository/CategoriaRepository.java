package com.jmcodestudio.papelaria.repository;

import com.jmcodestudio.papelaria.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // RN-38: navegação pública mostra apenas categorias de nível 1 (sem parent), ativas
    List<Categoria> findByParentIsNullAndAtivaTrueOrderByNome();

    List<Categoria> findByParentId(Long parentId);

    // RN-37: nome único dentro do mesmo nível (mesmo parent, ou ambos raiz)
    @Query("""
        SELECT c FROM Categoria c
        WHERE LOWER(c.nome) = LOWER(:nome)
        AND ((:parentId IS NULL AND c.parent IS NULL) OR c.parent.id = :parentId)
        AND (:excluindoId IS NULL OR c.id <> :excluindoId)
        """)
    Optional<Categoria> findConflitoDeNomeNoMesmoNivel(
            @Param("nome") String nome,
            @Param("parentId") Long parentId,
            @Param("excluindoId") Long excluindoId
    );
}

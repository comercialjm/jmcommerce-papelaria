package com.jmcodestudio.papelaria.repository;

import com.jmcodestudio.papelaria.entity.Pedido;
import com.jmcodestudio.papelaria.entity.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByStripeSessionId(String stripeSessionId);

    Optional<Pedido> findByNumero(String numero);

    // UC-08, RN-20: pedidos "esquecidos" no carrinho há mais de 30 min (job de expiração)
    List<Pedido> findByStatusAndCriadoEmBefore(StatusPedido status, LocalDateTime limite);

    // UC-16a: listagem do admin, com filtro opcional por status e busca por número/cliente
    @Query("""
            SELECT p FROM Pedido p
            WHERE (:status IS NULL OR p.status = :status)
            AND (:busca IS NULL OR :busca = ''
                 OR LOWER(p.numero) LIKE LOWER(CONCAT('%', :busca, '%'))
                 OR LOWER(p.clienteNome) LIKE LOWER(CONCAT('%', :busca, '%')))
            ORDER BY p.criadoEm DESC
            """)
    Page<Pedido> buscarParaAdmin(@Param("status") StatusPedido status, @Param("busca") String busca, Pageable pageable);

    // UC-13: estatísticas e pedidos recentes do dashboard
    long countByStatus(StatusPedido status);

    List<Pedido> findTop10ByOrderByCriadoEmDesc();
}

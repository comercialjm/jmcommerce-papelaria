package com.jmcodestudio.papelaria.repository;

import com.jmcodestudio.papelaria.entity.Pedido;
import com.jmcodestudio.papelaria.entity.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByStripeSessionId(String stripeSessionId);

    Optional<Pedido> findByNumero(String numero);

    // UC-08, RN-20: pedidos "esquecidos" no carrinho há mais de 30 min (job de expiração)
    List<Pedido> findByStatusAndCriadoEmBefore(StatusPedido status, LocalDateTime limite);
}

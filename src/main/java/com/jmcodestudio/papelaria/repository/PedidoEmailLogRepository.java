package com.jmcodestudio.papelaria.repository;

import com.jmcodestudio.papelaria.entity.PedidoEmailLog;
import com.jmcodestudio.papelaria.entity.TipoEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoEmailLogRepository extends JpaRepository<PedidoEmailLog, Long> {

    // RN-26: candidatos a retry — falharam, só tentaram 1x, e já passaram 5 min
    List<PedidoEmailLog> findByTipoAndSucessoFalseAndTentativasAndCriadoEmBefore(
            TipoEmail tipo, Integer tentativas, LocalDateTime limite);
}

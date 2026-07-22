package com.jmcodestudio.papelaria.repository;

import com.jmcodestudio.papelaria.entity.ConfiguracaoLoja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracaoLojaRepository extends JpaRepository<ConfiguracaoLoja, Long> {

    // Singleton: sempre existe exatamente uma linha (inserida na migration V1)
    Optional<ConfiguracaoLoja> findFirstByOrderByIdAsc();
}

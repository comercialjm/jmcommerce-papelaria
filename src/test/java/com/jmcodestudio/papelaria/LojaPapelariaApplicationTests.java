package com.jmcodestudio.papelaria;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class LojaPapelariaApplicationTests {

    @Test
    void contextLoads() {
        // Se o contexto do Spring subir sem erro, o setup básico está correto.
    }

}

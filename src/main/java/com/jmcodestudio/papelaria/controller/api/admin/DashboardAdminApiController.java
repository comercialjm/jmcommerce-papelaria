package com.jmcodestudio.papelaria.controller.api.admin;

import com.jmcodestudio.papelaria.dto.DashboardDTOs.Resumo;
import com.jmcodestudio.papelaria.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** UC-13: números do dashboard do admin. */
@RestController
@RequestMapping("/admin/api/dashboard")
@RequiredArgsConstructor
public class DashboardAdminApiController {

    private final DashboardService dashboardService;

    @GetMapping
    public Resumo resumo() {
        return dashboardService.montar();
    }

}

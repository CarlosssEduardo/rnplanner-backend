package br.com.rnplanner.controller;

import br.com.rnplanner.dto.DashboardDiaDTO;
import br.com.rnplanner.dto.ResumoMesDTO;
import br.com.rnplanner.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumo-do-dia/setor/{setor}")
    public ResponseEntity<DashboardDiaDTO> obterResumoDoDia(@PathVariable String setor) {

        return ResponseEntity.ok(dashboardService.obterResumoDoDia(setor));
    }

    @GetMapping("/resumo-mensal/setor/{setor}")
    public ResponseEntity<ResumoMesDTO> obterResumoMensal(@PathVariable String setor) {

        return ResponseEntity.ok(dashboardService.obterResumoMensal(setor));
    }
}
package br.com.rnplanner.controller;

import br.com.rnplanner.dto.LancamentoManualDTO;
import br.com.rnplanner.model.LancamentoManual;
import br.com.rnplanner.service.LancamentoManualService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lancamento-manual")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LancamentoManualController {

    private final LancamentoManualService service;

    @PostMapping("/salvar")
    public ResponseEntity<LancamentoManual> salvarLancamento(@RequestBody LancamentoManualDTO dto) {

        return ResponseEntity.ok(service.salvar(dto));
    }
}
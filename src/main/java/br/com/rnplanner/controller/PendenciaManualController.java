package br.com.rnplanner.controller;

import br.com.rnplanner.model.PendenciaManual;
import br.com.rnplanner.service.PendenciaManualService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pendencias-manuais")
@RequiredArgsConstructor
public class PendenciaManualController {


    private final PendenciaManualService service;

    @PostMapping("/salvar")
    public ResponseEntity<PendenciaManual> salvar(@RequestBody PendenciaManual pendencia) {

        return ResponseEntity.ok(service.salvar(pendencia));
    }

    @PutMapping("/resolver/{id}")
    public ResponseEntity<String> resolver(@PathVariable Long id) {
        service.resolver(id);
        return ResponseEntity.ok("Pendência resolvida!");
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<String> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.ok("Pendência removida!");
    }
}
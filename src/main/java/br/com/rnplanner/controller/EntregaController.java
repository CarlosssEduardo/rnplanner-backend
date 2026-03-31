package br.com.rnplanner.controller;

import br.com.rnplanner.service.EntregaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/entregas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EntregaController {


    private final EntregaService entregaService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {

            int rotasProcessadas = entregaService.importarCsv(file);


            return ResponseEntity.ok("Radar atualizado! 🚚 " + rotasProcessadas + " rotas processadas.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    @GetMapping("/rastreio/{pdvId}")
    public ResponseEntity<Map<String, Object>> buscarEntrega(@PathVariable Long pdvId) {


        return entregaService.buscarRastreio(pdvId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
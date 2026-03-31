package br.com.rnplanner.controller;

import br.com.rnplanner.dto.*;
import br.com.rnplanner.model.Visita;
import br.com.rnplanner.model.PendenciaManual;
import br.com.rnplanner.service.VisitaService;
import br.com.rnplanner.repository.PendenciaManualRepository;
import br.com.rnplanner.repository.LancamentoManualRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador REST responsável por orquestrar o ciclo de vida das visitas em campo.
 * Atua como a interface principal de comunicação com o Front-End (Mobile/Web) para registro de produtividade.
 */
@RestController
@RequestMapping("/visitas")
@CrossOrigin(origins = "*")
public class VisitaController {

    private final VisitaService visitaService;
    private final PendenciaManualRepository pendenciaManualRepository;
    private final LancamentoManualRepository lancamentoManualRepository;

    public VisitaController(VisitaService visitaService,
                            PendenciaManualRepository pendenciaManualRepository,
                            LancamentoManualRepository lancamentoManualRepository) {
        this.visitaService = visitaService;
        this.pendenciaManualRepository = pendenciaManualRepository;
        this.lancamentoManualRepository = lancamentoManualRepository;
    }

    /**
     * Inicia o atendimento em um cliente específico.
     * Se já existir uma visita em andamento para o dia vigente, retorna a visita existente para evitar duplicidade.
     *
     * @param pdvId Identificador único do Ponto de Venda.
     * @return Visita inicializada e salva no banco de dados.
     */
    @PostMapping("/iniciar/{pdvId}")
    public ResponseEntity<Visita> iniciarVisita(@PathVariable Long pdvId) {
        Visita visita = visitaService.iniciarVisita(pdvId);
        return ResponseEntity.ok(visita);
    }

    /**
     * Consolida o atendimento no PDV, gravando as métricas de execução e alterando o status para finalizado.
     *
     * @param id Identificador da visita em andamento.
     * @param dto Objeto DTO contendo todas as variáveis numéricas e anotações preenchidas no formulário do app.
     * @return Entidade Visita atualizada com os dados consolidados.
     */
    @PutMapping(value = "/{id}/finalizar", consumes = "application/json")
    public ResponseEntity<Visita> finalizar(@PathVariable Long id, @RequestBody FinalizarVisitaDTO dto) {
        Visita visitaFinalizada = visitaService.finalizarVisita(
                id, dto.getAnotacao(), dto.getQtdTasks(), dto.getQtdOfertas(), dto.getQtdMissoes(),
                dto.getQtdTasksCompra(), dto.getQtdTasksCerveja(), dto.getQtdTasksNab(), dto.getQtdTasksMkt(),
                dto.isVirouComprador(), dto.getQtdPositivacao()
        );
        return ResponseEntity.ok(visitaFinalizada);
    }

    /**
     * Recupera o consolidado diário de execução do vendedor.
     * * @param setor Setor logado no aplicativo.
     * @return Objeto com somatório de metas e execuções do dia.
     */
    @GetMapping("/dashboard/{setor}")
    public ResponseEntity<DashboardDiaDTO> obterDashboardGeral(@PathVariable String setor) {
        DashboardDiaDTO dashboard = visitaService.obterDashboardDoDiaPorSetor(setor);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Recupera o consolidado mensal do vendedor.
     * * @param setor Setor logado no aplicativo.
     * @return Objeto agrupando os dias trabalhados, resolução de pendências e histórico no mês.
     */
    @GetMapping("/dashboard/mes/{setor}")
    public ResponseEntity<ResumoMesDTO> obterDashboardMes(@PathVariable String setor) {
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fim = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        ResumoMesDTO resumoVisitas = visitaService.obterResumoMes(setor);
        long tasksHub;
        try {
            tasksHub = lancamentoManualRepository.sumTasksManuaisNoMes(inicio, fim, setor);
        } catch (Exception e) {
            tasksHub = 0L;
        }

        int totalTasks = resumoVisitas.getTotalTasksMes() + (int) tasksHub;

        return ResponseEntity.ok(new ResumoMesDTO(
                resumoVisitas.getDiasTrabalhados(),
                resumoVisitas.getProblemasResolvidos(),
                totalTasks,
                "Top 10 - CDD Belém"
        ));
    }

    @GetMapping
    public ResponseEntity<List<Visita>> listarTodas() {
        return ResponseEntity.ok(visitaService.listarTodas());
    }

    @GetMapping("/{id}/resumo")
    public ResponseEntity<VisitaRelatorioDTO> obterResumo(@PathVariable Long id) {
        return ResponseEntity.ok(visitaService.obterResumo(id));
    }

    @GetMapping("/{id}/itens-pendentes")
    public ResponseEntity<List<String>> obterItens(@PathVariable Long id) {
        return ResponseEntity.ok(visitaService.obterItensPendentes(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Visita> obterPorId(@PathVariable Long id) {
        return ResponseEntity.ok(visitaService.buscarPorId(id));
    }

    /**
     * Reúne todas as pendências e problemas reportados no setor, cruzando dados de Visitas Regulares e Anotações Manuais.
     *
     * @param setor Setor logado buscando o quadro de pendências.
     * @return Lista padronizada de DTOs contendo o status, descrição e origem de cada pendência.
     */
    @GetMapping("/pendencias/{setor}")
    public ResponseEntity<List<PendenciaDTO>> listarPendenciasGlobais(@PathVariable String setor) {
        List<PendenciaDTO> pendencias = new ArrayList<>(visitaService.listarPendenciasGlobaisPorSetor(setor));
        List<PendenciaManual> manuais = pendenciaManualRepository.findBySetor(setor);

        for (PendenciaManual pm : manuais) {
            PendenciaDTO dto = new PendenciaDTO();
            dto.setId("MANUAL-" + pm.getId());
            dto.setPdvId(0L);
            dto.setPdvNome("Anotação Avulsa");
            dto.setTexto(pm.getTexto());
            dto.setStatus(pm.getStatus() != null ? pm.getStatus() : "PENDENTE");
            pendencias.add(dto);
        }
        return ResponseEntity.ok(pendencias);
    }
}
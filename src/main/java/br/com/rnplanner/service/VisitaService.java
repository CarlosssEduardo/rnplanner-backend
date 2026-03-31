package br.com.rnplanner.service;

import br.com.rnplanner.dto.*;
import br.com.rnplanner.model.*;
import br.com.rnplanner.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serviço central que gerencia o fluxo de trabalho do vendedor em campo.
 * Intermedia o início, fim e compilação de produtividade de todas as visitas realizadas.
 */
@Service
public class VisitaService {

    private final VisitaRepository visitaRepository;
    private final PdvRepository pdvRepository;
    private final LancamentoManualRepository lancamentoManualRepository;

    public VisitaService(VisitaRepository visitaRepository,
                         PdvRepository pdvRepository,
                         LancamentoManualRepository lancamentoManualRepository) {
        this.visitaRepository = visitaRepository;
        this.pdvRepository = pdvRepository;
        this.lancamentoManualRepository = lancamentoManualRepository;
    }

    /**
     * Inicializa a visita para o PDV selecionado, garantindo o princípio de idempotência.
     * Se o usuário recarregar a tela, a visita anterior do dia é reaproveitada, evitando lixo no banco.
     *
     * @param pdvId Identificador único do PDV.
     * @return Entidade Visita recém-criada ou recuperada do histórico diário.
     */
    public Visita iniciarVisita(Long pdvId) {
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));

        Optional<Visita> visitaHoje = visitaRepository.findFirstByPdvIdUnicoAndDataOrderByIdDesc(pdvId, hoje);

        if (visitaHoje.isPresent()) {
            return visitaHoje.get();
        }

        Pdv pdv = pdvRepository.findById(pdvId).orElseThrow();
        Visita visita = new Visita();
        visita.setPdv(pdv);
        visita.setData(hoje);
        visita.setFinalizada(false);
        visita.setSetor(pdv.getSetor());
        return visitaRepository.save(visita);
    }

    /**
     * Finaliza a etapa de execução registrando os números alcançados e categorizando pendências operacionais.
     */
    public Visita finalizarVisita(Long id, String anotacao, int tasks, int ofertas, int missoes,
                                  int compra, int cerveja, int nab, int mkt, boolean comprador, int positivacao) {
        Visita visita = visitaRepository.findById(id).orElseThrow();
        visita.setObservacao(anotacao);
        visita.setQtdTasks(tasks);
        visita.setQtdOfertas(ofertas);
        visita.setQtdMissoes(missoes);
        visita.setQtdTasksCompra(compra);
        visita.setQtdTasksCerveja(cerveja);
        visita.setQtdTasksNab(nab);
        visita.setQtdTasksMkt(mkt);
        visita.setQtdPositivacao(positivacao);
        visita.setVirouComprador(comprador);

        // Inferência automatizada do status operacional baseada no conteúdo do formulário de anotações (JSON).
        if (anotacao == null || anotacao.trim().isEmpty() || anotacao.equals("[]")) {
            visita.setPendenciaStatus("SEM_PENDENCIA");
        } else if (anotacao.contains("\"status\":\"PENDENTE\"")) {
            visita.setPendenciaStatus("PENDENTE");
        } else {
            visita.setPendenciaStatus("RESOLVIDO");
        }

        visita.setFinalizada(true);
        return visitaRepository.save(visita);
    }

    /**
     * Compila o painel diário realizando o "Merge" de dados de duas fontes distintas:
     * O histórico de visitas finalizadas no aplicativo e os lançamentos manuais realizados através do Hub.
     *
     * @param setor Setor logado requisitando os dados.
     * @return Dashboard Dia DTO consolidado.
     */
    public DashboardDiaDTO obterDashboardDoDiaPorSetor(String setor) {
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));

        long tasksTotal = visitaRepository.sumTasksByDataAndSetor(hoje, setor) + lancamentoManualRepository.sumTasksManuais(hoje, setor);
        long ofertasTotal = visitaRepository.sumOfertasByDataAndSetor(hoje, setor) + lancamentoManualRepository.sumOfertasManuais(hoje, setor);
        long missoesTotal = visitaRepository.sumMissoesByDataAndSetor(hoje, setor) + lancamentoManualRepository.sumMissoesManuais(hoje, setor);
        long compra = visitaRepository.sumTasksCompraByDataAndSetor(hoje, setor) + lancamentoManualRepository.sumTasksCompraManuais(hoje, setor);
        long cerveja = visitaRepository.sumTasksCervejaByDataAndSetor(hoje, setor) + lancamentoManualRepository.sumTasksCervejaManuais(hoje, setor);
        long nab = visitaRepository.sumTasksNabByDataAndSetor(hoje, setor) + lancamentoManualRepository.sumTasksNabManuais(hoje, setor);
        long mkt = visitaRepository.sumTasksMktByDataAndSetor(hoje, setor) + lancamentoManualRepository.sumTasksMktManuais(hoje, setor);
        long compradores = visitaRepository.countCompradoresByDataAndSetor(hoje, setor) + lancamentoManualRepository.sumCompradoresManuais(hoje, setor);
        long positivacaoTotal = visitaRepository.sumPositivacaoByDataAndSetor(hoje, setor) + lancamentoManualRepository.sumPositivacaoManuais(hoje, setor);

        List<Pdv> pdvsDoSetor = pdvRepository.findBySetor(setor);
        boolean temPdf = !pdvsDoSetor.isEmpty();

        long metaTasksDia, metaMissoesDia, metaOfertasDia, metaCompradorDia, metaPositivacaoDia;
        long metaTasksCompraDia, metaTasksCervejaDia, metaTasksNabDia, metaTasksMktDia;

        // Se o setor não tiver base carregada (Pdf), injeta metas padrão de segurança para não quebrar os gráficos.
        if (temPdf) {
            metaTasksDia = pdvRepository.maxDesafioTasksBySetor(setor);
            metaMissoesDia = pdvRepository.maxDesafioMissoesBySetor(setor);
            metaOfertasDia = pdvRepository.maxDesafioOfertasBySetor(setor);
            metaCompradorDia = pdvRepository.maxMetaCompradorBySetor(setor);
            metaTasksCompraDia = pdvRepository.maxMetaTasksCompraBySetor(setor);
            metaTasksCervejaDia = pdvRepository.maxMetaTasksCervejaBySetor(setor);
            metaTasksNabDia = pdvRepository.maxMetaTasksNabBySetor(setor);
            metaTasksMktDia = pdvRepository.maxMetaTasksMktBySetor(setor);
            metaPositivacaoDia = pdvRepository.maxMetaPositivacaoBySetor(setor);
        } else {
            metaTasksDia = 40; metaMissoesDia = 10; metaOfertasDia = 10; metaCompradorDia = 2;
            metaTasksCompraDia = 10; metaTasksCervejaDia = 10; metaTasksNabDia = 10; metaTasksMktDia = 10; metaPositivacaoDia = 2;
        }

        List<Long> ids = visitaRepository.findPdvIdsVisitadosHojePorSetor(hoje, setor);

        return new DashboardDiaDTO(
                missoesTotal, tasksTotal, ofertasTotal, ids.size(),
                compra, cerveja, nab, mkt, compradores, positivacaoTotal,
                metaTasksDia, metaMissoesDia, metaOfertasDia, metaCompradorDia,
                metaTasksCompraDia, metaTasksCervejaDia, metaTasksNabDia, metaTasksMktDia, metaPositivacaoDia,
                ids
        );
    }

    public ResumoMesDTO obterResumoMes(String setor) {
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        LocalDate inicio = hoje.withDayOfMonth(1);
        LocalDate fim = hoje.withDayOfMonth(hoje.lengthOfMonth());
        int dias = (int) visitaRepository.countDiasTrabalhadosNoMesPorSetor(inicio, fim, setor);
        long tasks = visitaRepository.sumTasksNoMesPorSetor(inicio, fim, setor) + lancamentoManualRepository.sumTasksManuaisNoMes(inicio, fim, setor);
        int resolvidos = (int) visitaRepository.countProblemasResolvidosNoMesPorSetor(inicio, fim, setor);

        // TODO: Corrigir o problema da contagem de dias trabalhados que pode mascarar 0 dias como 1 se houver tasks órfãs.
        return new ResumoMesDTO(dias == 0 && tasks > 0 ? 1 : dias, resolvidos, (int) tasks, "Top 10 - CDD Belém");
    }

    public List<Visita> listarTodas() { return visitaRepository.findAll(); }
    public Visita buscarPorId(Long id) { return visitaRepository.findById(id).orElseThrow(); }

    public VisitaRelatorioDTO obterResumo(Long id) {
        Visita v = visitaRepository.findById(id).orElseThrow();
        return new VisitaRelatorioDTO(v.getPdv().getNome(), v.getObservacao(), v.getQtdTasks(), v.getQtdOfertas(), v.getQtdMissoes());
    }

    /**
     * Filtra e agrupa todas as pendências não resolvidas do setor mapeado.
     * Útil para o painel de administração da liderança.
     */
    public List<PendenciaDTO> listarPendenciasGlobaisPorSetor(String setor) {
        return visitaRepository.findAll().stream()
                .filter(v -> v.getSetor() != null && v.getSetor().equals(setor))
                .filter(v -> v.getObservacao() != null && !v.getObservacao().trim().isEmpty() && !v.getObservacao().equals("[]"))
                .map(v -> {
                    PendenciaDTO dto = new PendenciaDTO();
                    dto.setId(v.getId().toString());
                    dto.setPdvId(v.getPdv().getId());
                    dto.setPdvNome(v.getPdv().getNome());
                    dto.setTexto(v.getObservacao());
                    dto.setStatus(v.getPendenciaStatus() != null ? v.getPendenciaStatus() : "PENDENTE");
                    return dto;
                }).collect(Collectors.toList());
    }

    public List<String> obterItensPendentes(Long visitaId) {
        Visita v = visitaRepository.findById(visitaId).orElseThrow();
        List<String> itens = new ArrayList<>();
        String obs = v.getObservacao();
        // Quebra a anotação JSON armazenada de forma linear para devolver apenas os blocos de texto reportados.
        if (obs != null && obs.contains("[")) {
            String[] partes = obs.split("\"texto\":\"");
            for (int i = 1; i < partes.length; i++) { itens.add(partes[i].split("\"")[0]); }
        } else if (obs != null && !obs.trim().isEmpty()) {
            itens.add(obs);
        }
        return itens;
    }
}
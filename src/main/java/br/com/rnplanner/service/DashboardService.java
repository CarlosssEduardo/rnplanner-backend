package br.com.rnplanner.service;

import br.com.rnplanner.dto.DashboardDiaDTO;
import br.com.rnplanner.dto.ResumoMesDTO;
import br.com.rnplanner.repository.LancamentoManualRepository;
import br.com.rnplanner.repository.PendenciaManualRepository;
import br.com.rnplanner.repository.VisitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Serviço responsável por orquestrar e consolidar as métricas de performance dos vendedores.
 * Unifica os dados provenientes das visitas roteirizadas (App) e dos lançamentos avulsos (Hub Manual).
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final VisitaService visitaService;
    private final VisitaRepository visitaRepository;
    private final LancamentoManualRepository lancamentoManualRepository;
    private final PendenciaManualRepository pendenciaManualRepository;

    /**
     * Delega a busca do painel diário para o serviço de visitas.
     * * @param setor Código do setor do vendedor (ex: "501").
     * @return DTO contendo o compilado de metas e realizações do dia vigente.
     */
    public DashboardDiaDTO obterResumoDoDia(String setor) {
        return visitaService.obterDashboardDoDiaPorSetor(setor);
    }

    /**
     * Calcula os indicadores mensais do vendedor, processando a presença real e o volume total de entregáveis.
     *
     * @param setor Código do setor do vendedor logado.
     * @return DTO com os dias trabalhados, tasks, pendências resolvidas e a posição atual no ranking do CDD.
     */
    public ResumoMesDTO obterResumoMensal(String setor) {
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        LocalDate inicio = hoje.withDayOfMonth(1);
        LocalDate fim = hoje.withDayOfMonth(hoje.lengthOfMonth());

        // Utiliza uma coleção Set (não aceita duplicatas) para garantir que ações no App e no Hub
        // na mesma data contem como apenas 1 dia de presença real, evitando fraudes de produtividade.
        Set<LocalDate> diasAtivos = new HashSet<>();
        diasAtivos.addAll(visitaRepository.findDiasTrabalhados(inicio, fim, setor));
        diasAtivos.addAll(lancamentoManualRepository.findDiasTrabalhados(inicio, fim, setor));
        int diasReais = diasAtivos.size();

        long tasksApp = visitaRepository.sumTasksNoMesPorSetor(inicio, fim, setor);
        long tasksManuais = lancamentoManualRepository.sumTasksManuaisNoMes(inicio, fim, setor);
        long totalTasks = tasksApp + tasksManuais;

        int resolvidosApp = (int) visitaRepository.countProblemasResolvidosNoMesPorSetor(inicio, fim, setor);
        int resolvidosManuais = pendenciaManualRepository.findBySetorAndStatus(setor, "RESOLVIDO").size();
        int totalResolvidos = resolvidosApp + resolvidosManuais;

        String textoRanking = calcularPosicaoRanking(inicio, fim, setor);

        return new ResumoMesDTO(diasReais, totalResolvidos, (int) totalTasks, textoRanking);
    }

    /**
     * Constrói o ranking dinâmico do CDD baseado no volume absoluto de Tasks.
     * Agrupa a pontuação de todos os setores e localiza a posição do usuário solicitante.
     *
     * @param inicio Primeiro dia do mês vigente.
     * @param fim Último dia do mês vigente.
     * @param setorLogado Setor que deseja consultar sua própria posição.
     * @return String formatada com a posição (ex: "2º do RKG CDD Belém") ou status "S/N" caso não tenha pontuado.
     */
    private String calcularPosicaoRanking(LocalDate inicio, LocalDate fim, String setorLogado) {
        Map<String, Long> rankingMap = new HashMap<>();

        List<Object[]> tasksVisita = visitaRepository.sumTasksGroupedBySetor(inicio, fim);
        for (Object[] obj : tasksVisita) {
            String setor = (String) obj[0];
            Long tasks = (Long) obj[1];
            rankingMap.put(setor, rankingMap.getOrDefault(setor, 0L) + (tasks != null ? tasks : 0L));
        }

        List<Object[]> tasksManual = lancamentoManualRepository.sumTasksGroupedBySetor(inicio, fim);
        for (Object[] obj : tasksManual) {
            String setor = (String) obj[0];
            Long tasks = (Long) obj[1];
            rankingMap.put(setor, rankingMap.getOrDefault(setor, 0L) + (tasks != null ? tasks : 0L));
        }

        if (rankingMap.isEmpty() || !rankingMap.containsKey(setorLogado)) {
            return "S/N do RKG CDD Belém";
        }

        // Ordena o mapa transformando-o em uma lista, do maior pontuador para o menor (Ordem Decrescente).
        List<Map.Entry<String, Long>> listaOrdenada = new ArrayList<>(rankingMap.entrySet());
        listaOrdenada.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        int posicao = 1;
        for (Map.Entry<String, Long> entry : listaOrdenada) {
            if (entry.getKey().equals(setorLogado)) {
                return posicao + "º do RKG CDD Belém";
            }
            posicao++;
        }

        return "S/N do RKG CDD Belém";
    }
}
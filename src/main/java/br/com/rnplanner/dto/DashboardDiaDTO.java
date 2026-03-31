package br.com.rnplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDiaDTO {

    private long missoesTotal;
    private long tasksTotal;
    private long ofertasTotal;
    private long pdvsVisitados;
    private long tasksCompraTotal;
    private long tasksCervejaTotal;
    private long tasksNabTotal;
    private long tasksMktTotal;
    private long compradoresTotal;
    private long positivacaoTotal;


    private long metaTasksDia;
    private long metaMissoesDia;
    private long metaOfertasDia;
    private long metaCompradorDia;

    private long metaTasksCompraDia;
    private long metaTasksCervejaDia;
    private long metaTasksNabDia;
    private long metaTasksMktDia;

    private long metaPositivacaoDia;

    private List<Long> pdvsVisitadosIds;
}
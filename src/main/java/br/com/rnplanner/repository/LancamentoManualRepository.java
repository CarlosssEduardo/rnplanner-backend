package br.com.rnplanner.repository;

import br.com.rnplanner.model.LancamentoManual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;

@Repository
public interface LancamentoManualRepository extends JpaRepository<LancamentoManual, Long> {

    @Query("SELECT COALESCE(SUM(l.tasks), 0) FROM LancamentoManual l WHERE l.data = :data AND l.setor = :setor")
    long sumTasksManuais(@Param("data") LocalDate data, @Param("setor") String setor);

    @Query("SELECT COALESCE(SUM(l.ofertas), 0) FROM LancamentoManual l WHERE l.data = :data AND l.setor = :setor")
    long sumOfertasManuais(@Param("data") LocalDate data, @Param("setor") String setor);

    @Query("SELECT COALESCE(SUM(l.missoes), 0) FROM LancamentoManual l WHERE l.data = :data AND l.setor = :setor")
    long sumMissoesManuais(@Param("data") LocalDate data, @Param("setor") String setor);

    @Query("SELECT COALESCE(SUM(l.tasks), 0) FROM LancamentoManual l WHERE l.data >= :inicio AND l.data <= :fim AND l.setor = :setor")
    long sumTasksManuaisNoMes(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim, @Param("setor") String setor);

    @Query("SELECT COALESCE(SUM(l.tasksCompra), 0) FROM LancamentoManual l WHERE l.data = :data AND l.setor = :setor")
    long sumTasksCompraManuais(@Param("data") LocalDate data, @Param("setor") String setor);

    @Query("SELECT COALESCE(SUM(l.tasksCerveja), 0) FROM LancamentoManual l WHERE l.data = :data AND l.setor = :setor")
    long sumTasksCervejaManuais(@Param("data") LocalDate data, @Param("setor") String setor);

    @Query("SELECT COALESCE(SUM(l.tasksNab), 0) FROM LancamentoManual l WHERE l.data = :data AND l.setor = :setor")
    long sumTasksNabManuais(@Param("data") LocalDate data, @Param("setor") String setor);

    @Query("SELECT COALESCE(SUM(l.tasksMkt), 0) FROM LancamentoManual l WHERE l.data = :data AND l.setor = :setor")
    long sumTasksMktManuais(@Param("data") LocalDate data, @Param("setor") String setor);

    @Query("SELECT COALESCE(SUM(l.qtdCompradores), 0) FROM LancamentoManual l WHERE l.data = :data AND l.setor = :setor")
    long sumCompradoresManuais(@Param("data") LocalDate data, @Param("setor") String setor);

    @Query("SELECT COALESCE(SUM(l.qtdPositivacao), 0) FROM LancamentoManual l WHERE l.setor = :setor AND l.data = :data")
    long sumPositivacaoManuais(@Param("data") LocalDate data, @Param("setor") String setor);

    @Query("SELECT DISTINCT l.data FROM LancamentoManual l WHERE l.setor = :setor AND l.data BETWEEN :inicio AND :fim")
    List<LocalDate> findDiasTrabalhados(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim, @Param("setor") String setor);

    @Query("SELECT l.setor, SUM(l.tasks) FROM LancamentoManual l WHERE l.data BETWEEN :inicio AND :fim GROUP BY l.setor")
    List<Object[]> sumTasksGroupedBySetor(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
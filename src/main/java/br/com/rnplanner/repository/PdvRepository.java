package br.com.rnplanner.repository;

import br.com.rnplanner.model.Pdv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PdvRepository extends JpaRepository<Pdv, Long> {

    List<Pdv> findBySetor(String setor);
    boolean existsBySetor(String setor);
    void deleteBySetorIn(List<String> setores);

    @Query("SELECT COALESCE(MAX(p.desafioTasks), 0) FROM Pdv p WHERE p.setor = :setor")
    long maxDesafioTasksBySetor(@Param("setor") String setor);

    @Query("SELECT COALESCE(MAX(p.desafioMissoes), 0) FROM Pdv p WHERE p.setor = :setor")
    long maxDesafioMissoesBySetor(@Param("setor") String setor);

    @Query("SELECT COALESCE(MAX(p.desafioOfertas), 0) FROM Pdv p WHERE p.setor = :setor")
    long maxDesafioOfertasBySetor(@Param("setor") String setor);

    @Query("SELECT COALESCE(MAX(p.metaTasksCompra), 0) FROM Pdv p WHERE p.setor = :setor")
    long maxMetaTasksCompraBySetor(@Param("setor") String setor);

    @Query("SELECT COALESCE(MAX(p.metaTasksCerveja), 0) FROM Pdv p WHERE p.setor = :setor")
    long maxMetaTasksCervejaBySetor(@Param("setor") String setor);

    @Query("SELECT COALESCE(MAX(p.metaTasksNab), 0) FROM Pdv p WHERE p.setor = :setor")
    long maxMetaTasksNabBySetor(@Param("setor") String setor);

    @Query("SELECT COALESCE(MAX(p.metaTasksMkt), 0) FROM Pdv p WHERE p.setor = :setor")
    long maxMetaTasksMktBySetor(@Param("setor") String setor);

    @Query("SELECT COALESCE(MAX(p.metaComprador), 0) FROM Pdv p WHERE p.setor = :setor")
    long maxMetaCompradorBySetor(@Param("setor") String setor);

    @Query("SELECT COALESCE(MAX(p.metaPositivacao), 0) FROM Pdv p WHERE p.setor = :setor")
    long maxMetaPositivacaoBySetor(@Param("setor") String setor);
}
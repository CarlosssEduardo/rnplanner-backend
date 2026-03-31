package br.com.rnplanner.repository;

import br.com.rnplanner.model.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    Optional<Entrega> findFirstByPdvId(Long pdvId);
}
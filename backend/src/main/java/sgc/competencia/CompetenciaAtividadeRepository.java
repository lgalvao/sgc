package sgc.competencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório JPA para a associação CompetenciaAtividade (N-N).
 * Usa a chave composta CompetenciaAtividade.Id.

 */
@Repository
public interface CompetenciaAtividadeRepository extends JpaRepository<CompetenciaAtividade, CompetenciaAtividade.Id> {
}
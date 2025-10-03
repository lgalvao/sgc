package sgc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sgc.model.CompetenciaAtividade;

/**
 * Repositório JPA para a associação CompetenciaAtividade (N-N).
 * Usa a chave composta CompetenciaAtividade.Id.
 * Nomes e documentação em português conforme solicitado.
 */
@Repository
public interface CompetenciaAtividadeRepository extends JpaRepository<CompetenciaAtividade, CompetenciaAtividade.Id> {
}
package sgc.competencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório JPA para a entidade Competencia.
 */
@Repository
public interface CompetenciaRepository extends JpaRepository<Competencia, Long> {
}

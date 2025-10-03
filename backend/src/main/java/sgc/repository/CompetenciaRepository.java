package sgc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sgc.model.Competencia;

/**
 * Repositório JPA para a entidade Competencia.
 * Todos os nomes de classes e endpoints serão mantidos em português onde fizer sentido.
 */
@Repository
public interface CompetenciaRepository extends JpaRepository<Competencia, Long> {
}
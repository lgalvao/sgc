package sgc.unidade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório JPA para a entidade Unidade.

 */
@Repository
public interface UnidadeRepository extends JpaRepository<Unidade, Long> {
}
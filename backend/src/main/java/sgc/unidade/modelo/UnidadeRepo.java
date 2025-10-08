package sgc.unidade.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório JPA para a entidade Unidade.

 */
@Repository
public interface UnidadeRepo extends JpaRepository<Unidade, Long> {
}
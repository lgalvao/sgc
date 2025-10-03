package sgc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sgc.model.Unidade;

/**
 * Repositório JPA para a entidade Unidade.
 * Nomes e documentação em português conforme solicitado.
 */
@Repository
public interface UnidadeRepository extends JpaRepository<Unidade, Long> {
}
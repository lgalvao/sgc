package sgc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sgc.model.Conhecimento;

/**
 * Repositório JPA para a entidade Conhecimento.
 * Nomes e documentação em português conforme solicitado.
 */
@Repository
public interface ConhecimentoRepository extends JpaRepository<Conhecimento, Long> {
}
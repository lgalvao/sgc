package sgc.conhecimento.modelo;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
 
/**
 * Repositório JPA para a entidade Conhecimento.

 */
@Repository
public interface ConhecimentoRepo extends JpaRepository<Conhecimento, Long> {
    /**
     * Recupera conhecimentos vinculados a uma atividade.
     *
     * @param atividadeCodigo id da atividade
     * @return lista de Conhecimento
     */
    List<Conhecimento> findByAtividadeCodigo(Long atividadeCodigo);
}
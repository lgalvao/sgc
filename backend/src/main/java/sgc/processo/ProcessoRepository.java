package sgc.processo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para a entidade Processo.
 */
@Repository
public interface ProcessoRepository extends JpaRepository<Processo, Long> {
    
    /**
     * Busca processos por situação.
     *
     * @param situacao Situação do processo (CRIADO, EM_ANDAMENTO, FINALIZADO)
     * @return Lista de processos com a situação especificada
     */
    List<Processo> findBySituacao(String situacao);
}
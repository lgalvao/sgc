package sgc.processo.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sgc.processo.SituacaoProcesso;

import java.util.List;



/**

 * Repositório JPA para a entidade Processo.

 */

@Repository

public interface ProcessoRepo extends JpaRepository<Processo, Long> {

    /**

     * Busca processos por situação.

     *

     * @param situacao Situação do processo (CRIADO, EM_ANDAMENTO, FINALIZADO)

     * @return Lista de processos com a situação especificada

     */

    List<Processo> findBySituacao(SituacaoProcesso situacao);

}

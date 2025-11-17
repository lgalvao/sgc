package sgc.processo.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessoRepo extends JpaRepository<Processo, Long> {
    /**
     * Busca processos por situação.
     *
     * @param situacao Situação do processo (CRIADO, EM_ANDAMENTO, FINALIZADO)
     * @return Lista de processos com a situação especificada
     */

    List<Processo> findBySituacao(SituacaoProcesso situacao);

    Page<Processo> findDistinctByParticipantes_CodigoIn(List<Long> codigos, Pageable pageable);
}

package sgc.processo.model;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

    Page<Processo> findDistinctByParticipantes_CodigoInAndSituacaoNot(
            List<Long> codigos, SituacaoProcesso situacao, Pageable pageable);
}

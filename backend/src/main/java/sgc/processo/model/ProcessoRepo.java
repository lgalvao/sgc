package sgc.processo.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    Page<Processo> findDistinctByParticipantes_CodigoInAndSituacaoNot(
            List<Long> codigos, SituacaoProcesso situacao, Pageable pageable);

    @Query("SELECT distinct u.codigo FROM Processo p JOIN p.participantes u "
            + "WHERE p.situacao = :situacao AND u.codigo IN :codigos")
    List<Long> findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(
            @Param("situacao") SituacaoProcesso situacao,
            @Param("codigos") List<Long> codigos);

    @Query("SELECT distinct u.codigo FROM Processo p JOIN p.participantes u "
            + "WHERE p.situacao = :situacao AND p.tipo = :tipo")
    List<Long> findUnidadeCodigosBySituacaoAndTipo(
            @Param("situacao") SituacaoProcesso situacao,
            @Param("tipo") TipoProcesso tipo);
}

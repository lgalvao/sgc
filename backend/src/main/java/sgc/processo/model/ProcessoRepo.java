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
    @Query("""
            SELECT DISTINCT p FROM Processo p LEFT JOIN FETCH p.participantes WHERE p.situacao = :situacao
            """)
    List<Processo> findBySituacao(@Param("situacao") SituacaoProcesso situacao);

    @Query("""
            SELECT DISTINCT p FROM Processo p LEFT JOIN FETCH p.participantes
            WHERE p.situacao = :situacao
            ORDER BY p.dataFinalizacao DESC
            """)
    List<Processo> listarPorSituacaoComParticipantes(@Param("situacao") SituacaoProcesso situacao);

    /**
     * Busca processos onde as unidades participantes incluem as unidades especificadas
     * e o processo não está na situação especificada.
     * 
     * Usando LEFT JOIN FETCH para eager load dos participantes para resolver issue
     * onde query JPQL com INNER JOIN não retornava resultados.
     * 
     * @param codigos Lista de códigos de unidades participantes
     * @param situacao Situação a ser excluída
     * @param pageable Informações de paginação
     * @return Página de processos que atendem aos critérios
     */
    @Query(value = """
            SELECT DISTINCT p FROM Processo p
            LEFT JOIN FETCH p.participantes up
            WHERE up.id.unidadeCodigo IN :codigos
            AND p.situacao <> :situacao
            """,
            countQuery = """
            SELECT COUNT(DISTINCT p) FROM Processo p
            LEFT JOIN p.participantes up
            WHERE up.id.unidadeCodigo IN :codigos
            AND p.situacao <> :situacao
            """)
    Page<Processo> findDistinctByParticipantes_IdUnidadeCodigoInAndSituacaoNot(
            @Param("codigos") List<Long> codigos,
            @Param("situacao") SituacaoProcesso situacao,
            Pageable pageable);

    @Query("""
            SELECT distinct u.id.unidadeCodigo FROM Processo p JOIN p.participantes u
            WHERE p.situacao = :situacao AND u.id.unidadeCodigo IN :codigos
            """)
    List<Long> findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(
            @Param("situacao") SituacaoProcesso situacao,
            @Param("codigos") List<Long> codigos);

    @Query("""
            SELECT distinct u.id.unidadeCodigo FROM Processo p JOIN p.participantes u
            WHERE p.situacao = :situacao AND p.tipo = :tipo
            """)
    List<Long> findUnidadeCodigosBySituacaoAndTipo(
            @Param("situacao") SituacaoProcesso situacao,
            @Param("tipo") TipoProcesso tipo);

    @Query("""
            SELECT distinct u.id.unidadeCodigo FROM Processo p JOIN p.participantes u
            WHERE p.situacao IN :situacoes AND (:idIgnorado IS NULL OR p.codigo <> :idIgnorado)
            """)
    List<Long> findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
            @Param("situacoes") List<SituacaoProcesso> situacoes,
            @Param("idIgnorado") Long idIgnorado);
}

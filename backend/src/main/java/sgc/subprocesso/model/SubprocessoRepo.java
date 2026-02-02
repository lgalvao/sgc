package sgc.subprocesso.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para a entidade Subprocesso. Inclui query com fetch join para evitar N+1 ao
 * carregar unidade associada.
 */
@Repository
public interface SubprocessoRepo extends JpaRepository<Subprocesso, Long> {
    @Query("""
            select s from Subprocesso s
              join fetch s.unidade u
              left join fetch s.mapa m
            where s.processo.codigo = :codProcesso""")
    List<Subprocesso> findByProcessoCodigoWithUnidade(@Param("codProcesso") Long codProcesso);

    @Query("""
            select s from Subprocesso s
              join fetch s.unidade u
            where s.processo.codigo = :codProcesso
              and s.situacao = :situacao""")
    List<Subprocesso> findByProcessoCodigoAndSituacaoWithUnidade(
            @Param("codProcesso") Long codProcesso,
            @Param("situacao") SituacaoSubprocesso situacao);

    @Query("""
            select s from Subprocesso s
              join fetch s.unidade u
            where s.processo.codigo = :codProcesso
              and s.unidade.codigo = :codUnidade
              and s.situacao in :situacoes""")
    List<Subprocesso> findByProcessoCodigoAndUnidadeCodigoAndSituacaoInWithUnidade(
            @Param("codProcesso") Long codProcesso,
            @Param("codUnidade") Long codUnidade,
            @Param("situacoes") List<SituacaoSubprocesso> situacoes);

    @Query("""
            select s from Subprocesso s
              join fetch s.unidade u
            where s.processo.codigo = :codProcesso
              and s.unidade.codigo in :unidadeCodigos""")
    List<Subprocesso> findByProcessoCodigoAndUnidadeCodigoInWithUnidade(
            @Param("codProcesso") Long codProcesso,
            @Param("unidadeCodigos") List<Long> unidadeCodigos);

    @Query("""
            select s from Subprocesso s
              join fetch s.unidade u
            where s.processo.codigo = :codProcesso
              and s.situacao in :situacoes""")
    List<Subprocesso> findByProcessoCodigoAndSituacaoInWithUnidade(
            @Param("codProcesso") Long codProcesso,
            @Param("situacoes") List<SituacaoSubprocesso> situacoes);

    @Query("""
            SELECT s FROM Subprocesso s JOIN FETCH s.processo JOIN FETCH s.unidade LEFT JOIN FETCH s.mapa
            """)
    List<Subprocesso> findAllComFetch();

    List<Subprocesso> findByProcessoCodigo(Long processoCodigo);

    Optional<Subprocesso> findByMapaCodigo(Long mapaCodigo);

    Optional<Subprocesso> findByProcessoCodigoAndUnidadeCodigo(Long processoCodigo, Long unidadeCodigo);

    boolean existsByProcessoCodigoAndUnidadeCodigoIn(Long processoCodigo, List<Long> unidadesCodigos);

    List<Subprocesso> findBySituacao(SituacaoSubprocesso situacao);

    /**
     * Conta o total de subprocessos de um processo.
     *
     * @param processoCodigo código do processo
     * @return quantidade de subprocessos
     */
    long countByProcessoCodigo(Long processoCodigo);

    /**
     * Conta quantos subprocessos de um processo estão nas situações especificadas.
     *
     * @param processoCodigo código do processo
     * @param situacoes      situações a verificar
     * @return quantidade de subprocessos nas situações
     */
    long countByProcessoCodigoAndSituacaoIn(Long processoCodigo, List<SituacaoSubprocesso> situacoes);
}

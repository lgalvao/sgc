package sgc.subprocesso.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

/**
 * Repositório JPA para a entidade Subprocesso. Inclui query com fetch join para evitar N+1 ao
 * carregar unidade associada.
 */
@Repository
public interface SubprocessoRepo extends JpaRepository<Subprocesso, Long> {
    @Query("""
            select s from Subprocesso s
              join fetch s.processo p
              join fetch s.unidade u
              left join fetch s.mapa m
            where s.processo.codigo = :codProcesso""")
    List<Subprocesso> findByProcessoCodigoWithUnidade(@Param("codProcesso") Long codProcesso);

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
            SELECT DISTINCT s FROM Subprocesso s
            JOIN FETCH s.processo p
            JOIN FETCH s.unidade u
            LEFT JOIN FETCH s.mapa m
            LEFT JOIN FETCH m.atividades a
            WHERE s.codigo = :codigo
            """)
    Optional<Subprocesso> findByIdWithMapaAndAtividades(@Param("codigo") Long codigo);

    @Query("""
            SELECT s FROM Subprocesso s JOIN FETCH s.processo JOIN FETCH s.unidade LEFT JOIN FETCH s.mapa
            """)
    List<Subprocesso> findAllComFetch();

    List<Subprocesso> findByProcessoCodigo(Long processoCodigo);

    @Query("""
            SELECT DISTINCT s FROM Subprocesso s
            JOIN FETCH s.processo
            JOIN FETCH s.unidade
            LEFT JOIN FETCH s.mapa m
            LEFT JOIN FETCH m.atividades
            WHERE s.processo.codigo = :codProcesso AND s.unidade.codigo = :codUnidade
            """)
    Optional<Subprocesso> findByProcessoCodigoAndUnidadeCodigoWithFetch(
            @Param("codProcesso") Long codProcesso,
            @Param("codUnidade") Long codUnidade);

    Optional<Subprocesso> findByMapa_Codigo(Long mapaCodigo);

    Optional<Subprocesso> findByProcessoCodigoAndUnidadeCodigo(Long processoCodigo, Long unidadeCodigo);

    boolean existsByProcessoCodigoAndUnidadeCodigoIn(Long processoCodigo, List<Long> unidadesCodigos);

    List<Subprocesso> findBySituacao(SituacaoSubprocesso situacao);

    long countByProcessoCodigo(Long processoCodigo);

    long countByProcessoCodigoAndSituacaoIn(Long processoCodigo, List<SituacaoSubprocesso> situacoes);
}

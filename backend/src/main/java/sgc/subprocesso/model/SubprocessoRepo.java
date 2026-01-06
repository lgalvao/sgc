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

    @Query("SELECT s FROM Subprocesso s JOIN FETCH s.processo JOIN FETCH s.unidade LEFT JOIN FETCH s.mapa")
    List<Subprocesso> findAllComFetch();

    List<Subprocesso> findByProcessoCodigo(Long processoCodigo);

    Optional<Subprocesso> findByMapaCodigo(Long mapaCodigo);

    Optional<Subprocesso> findByProcessoCodigoAndUnidadeCodigo(Long processoCodigo, Long unidadeCodigo);

    boolean existsByProcessoCodigoAndUnidadeCodigoIn(Long processoCodigo, List<Long> unidadesCodigos);

    List<Subprocesso> findBySituacao(SituacaoSubprocesso situacao);
}

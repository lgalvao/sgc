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
    @Query(
            "select s from Subprocesso s join fetch s.unidade u left join fetch s.mapa m where"
                    + " s.processo.codigo = :codProcesso")
    List<Subprocesso> findByProcessoCodigoWithUnidade(@Param("codProcesso") Long codProcesso);

    @Override
    @Query("SELECT s FROM Subprocesso s JOIN FETCH s.processo JOIN FETCH s.unidade LEFT JOIN FETCH s.mapa")
    List<Subprocesso> findAll();

    List<Subprocesso> findByProcessoCodigo(Long processoCodigo);

    boolean existsByProcessoCodigoAndUnidadeCodigo(Long processoCodigo, Long unidadeCodigo);

    Optional<Subprocesso> findByProcessoCodigoAndUnidadeCodigo(
            Long processoCodigo, Long unidadeCodigo);

    Optional<Subprocesso> findByMapaCodigo(Long mapaCodigo);

    List<Subprocesso> findByUnidadeCodigo(Long unidadeCodigo);

    /**
     * Verifica se existe subprocesso para um processo e qualquer uma das unidades fornecidas.
     * Útil para verificação hierárquica de permissões.
     *
     * @param processoCodigo código do processo
     * @param unidadesCodigos lista de códigos de unidades
     * @return true se existir pelo menos um subprocesso
     */
    boolean existsByProcessoCodigoAndUnidadeCodigoIn(Long processoCodigo, List<Long> unidadesCodigos);

    List<Subprocesso> findBySituacao(SituacaoSubprocesso situacao);
}

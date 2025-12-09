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
            "select s from Subprocesso s join fetch s.unidade u where s.processo.codigo ="
                    + " :codProcesso")
    List<Subprocesso> findByProcessoCodigoWithUnidade(@Param("codProcesso") Long codProcesso);

    List<Subprocesso> findByProcessoCodigo(Long processoCodigo);

    boolean existsByProcessoCodigoAndUnidadeCodigo(Long processoCodigo, Long unidadeCodigo);

    Optional<Subprocesso> findByProcessoCodigoAndUnidadeCodigo(
            Long processoCodigo, Long unidadeCodigo);

    Optional<Subprocesso> findByMapaCodigo(Long mapaCodigo);

    List<Subprocesso> findByUnidadeCodigo(Long unidadeCodigo);
}

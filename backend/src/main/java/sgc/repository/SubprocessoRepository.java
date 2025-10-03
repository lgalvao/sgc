package sgc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sgc.model.Subprocesso;

import java.util.List;

/**
 * Repositório JPA para a entidade Subprocesso.
 * Inclui query com fetch join para evitar N+1 ao carregar unidade associada.
 */
@Repository
public interface SubprocessoRepository extends JpaRepository<Subprocesso, Long> {

    @Query("select s from Subprocesso s join fetch s.unidade u where s.processo.codigo = :processoCodigo")
    List<Subprocesso> findByProcessoCodigoWithUnidade(@Param("processoCodigo") Long processoCodigo);

    List<Subprocesso> findByProcessoCodigo(Long processoCodigo);
}
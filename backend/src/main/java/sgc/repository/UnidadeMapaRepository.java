package sgc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sgc.model.UnidadeMapa;

import java.util.Optional;

/**
 * Repositório para UNIDADE_MAPA permitindo localizar o mapa vigente por unidade.
 */
@Repository
public interface UnidadeMapaRepository extends JpaRepository<UnidadeMapa, UnidadeMapa.Id> {

    @Query("select um from UnidadeMapa um where um.unidade.codigo = :unidadeCodigo")
    Optional<UnidadeMapa> findByUnidadeCodigo(@Param("unidadeCodigo") Long unidadeCodigo);
}
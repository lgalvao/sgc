package sgc.unidade.api.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para a entidade Unidade.
 */
@Repository
public interface UnidadeRepo extends JpaRepository<Unidade, Long> {
    Optional<Unidade> findBySigla(String sigla);

    @Query("SELECT u.sigla FROM Unidade u WHERE u.codigo IN :codigos")
    List<String> findSiglasByCodigos(@Param("codigos") List<Long> codigos);

    @Query("SELECT u FROM Unidade u LEFT JOIN FETCH u.unidadeSuperior")
    List<Unidade> findAllWithHierarquia();

    List<Unidade> findByUnidadeSuperiorCodigo(Long unidadeSuperiorCodigo);

    List<Unidade> findByTituloTitular(String tituloTitular);

    @Query(value = """
        WITH RECURSIVE hierarquia AS (
            SELECT codigo
            FROM sgc.vw_unidade
            WHERE codigo = :codigo
            UNION ALL
            SELECT u.codigo
            FROM sgc.vw_unidade u
            INNER JOIN hierarquia h ON u.unidade_superior_codigo = h.codigo
        )
        SELECT codigo FROM hierarquia
        """, nativeQuery = true)
    List<Long> findCodigosDescendentes(@Param("codigo") Long codigo);
}

package sgc.unidade.internal.model;

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
}

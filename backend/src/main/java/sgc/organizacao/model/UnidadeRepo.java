package sgc.organizacao.model;

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
    Optional<Unidade> findBySiglaAndSituacao(String sigla, SituacaoUnidade situacao);

    default Optional<Unidade> findBySigla(String sigla) {
        return findBySiglaAndSituacao(sigla, SituacaoUnidade.ATIVA);
    }

    @Query("""
            SELECT u.sigla FROM Unidade u
            WHERE u.codigo IN :codigos
            AND u.situacao = sgc.organizacao.model.SituacaoUnidade.ATIVA
            """)
    List<String> findSiglasByCodigos(@Param("codigos") List<Long> codigos);

    @Query("""
            SELECT u FROM Unidade u
            LEFT JOIN FETCH u.unidadeSuperior
            WHERE u.situacao = sgc.organizacao.model.SituacaoUnidade.ATIVA
            """)
    List<Unidade> findAllWithHierarquia();

    List<Unidade> findByUnidadeSuperiorCodigoAndSituacao(Long unidadeSuperiorCodigo, SituacaoUnidade situacao);

    default List<Unidade> findByUnidadeSuperiorCodigo(Long unidadeSuperiorCodigo) {
        return findByUnidadeSuperiorCodigoAndSituacao(unidadeSuperiorCodigo, SituacaoUnidade.ATIVA);
    }

    List<Unidade> findByTituloTitularAndSituacao(String tituloTitular, SituacaoUnidade situacao);

    default List<Unidade> findByTituloTitular(String tituloTitular) {
        return findByTituloTitularAndSituacao(tituloTitular, SituacaoUnidade.ATIVA);
    }
}

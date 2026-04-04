package sgc.organizacao.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

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
            SELECT u.codigo FROM Unidade u
            WHERE UPPER(u.sigla) = UPPER(:sigla)
            AND u.situacao = SituacaoUnidade.ATIVA
            """)
    Optional<Long> buscarCodigoAtivoPorSigla(@Param("sigla") String sigla);

    @Query("""
            SELECT u.sigla FROM Unidade u
            WHERE u.codigo IN :codigos
            AND u.situacao = SituacaoUnidade.ATIVA
            """)
    List<String> buscarSiglasPorCodigos(@Param("codigos") List<Long> codigos);

    @Query("""
            SELECT u.sigla FROM Unidade u
            WHERE u.codigo = :codigo
            AND u.situacao = SituacaoUnidade.ATIVA
            """)
    Optional<String> buscarSiglaPorCodigo(@Param("codigo") Long codigo);

    @Query("""
            SELECT new sgc.organizacao.model.UnidadeHierarquiaLeitura(
                u.codigo,
                u.nome,
                u.sigla,
                u.tituloTitular,
                u.tipo,
                u.situacao,
                u.unidadeSuperior.codigo
            )
            FROM Unidade u
            WHERE u.situacao = SituacaoUnidade.ATIVA
            """)
    List<UnidadeHierarquiaLeitura> listarEstruturasAtivas();

    @Query("""
            SELECT u FROM Unidade u
            LEFT JOIN FETCH u.unidadeSuperior
            WHERE u.situacao = SituacaoUnidade.ATIVA
            """)
    List<Unidade> listarAtivasComSuperior();

    @Query("""
            SELECT u FROM Unidade u
            LEFT JOIN FETCH u.unidadeSuperior
            LEFT JOIN FETCH u.responsabilidade
            LEFT JOIN FETCH u.responsabilidade.usuario
            WHERE u.codigo = :codigo
            AND u.situacao = SituacaoUnidade.ATIVA
            """)
    Optional<Unidade> buscarPorCodigoComResponsavel(@Param("codigo") Long codigo);

    @Query("""
            SELECT u FROM Unidade u
            LEFT JOIN FETCH u.unidadeSuperior
            WHERE u.codigo = :codigo
            AND u.situacao = SituacaoUnidade.ATIVA
            """)
    Optional<Unidade> buscarPorCodigoComSuperior(@Param("codigo") Long codigo);

    @Query("""
            SELECT u FROM Unidade u
            LEFT JOIN FETCH u.unidadeSuperior
            LEFT JOIN FETCH u.responsabilidade
            LEFT JOIN FETCH u.responsabilidade.usuario
            WHERE UPPER(u.sigla) = UPPER(:sigla)
            AND u.situacao = SituacaoUnidade.ATIVA
            """)
    Optional<Unidade> buscarPorSiglaComResponsavel(@Param("sigla") String sigla);

    List<Unidade> findByUnidadeSuperiorCodigoAndSituacao(Long unidadeSuperiorCodigo, SituacaoUnidade situacao);

    default List<Unidade> findByUnidadeSuperiorCodigo(Long unidadeSuperiorCodigo) {
        return findByUnidadeSuperiorCodigoAndSituacao(unidadeSuperiorCodigo, SituacaoUnidade.ATIVA);
    }

    List<Unidade> findByTituloTitularAndSituacao(String tituloTitular, SituacaoUnidade situacao);

    default List<Unidade> findByTituloTitular(String tituloTitular) {
        return findByTituloTitularAndSituacao(tituloTitular, SituacaoUnidade.ATIVA);
    }
}

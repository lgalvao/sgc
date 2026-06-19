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
            SELECT u.codigo FROM Unidade u
            WHERE UPPER(u.sigla) = UPPER(:sigla)
            AND u.situacao = sgc.organizacao.model.SituacaoUnidade.ATIVA
            """)
    Optional<Long> buscarCodigoAtivoPorSigla(@Param("sigla") String sigla);

    @Query("""
            SELECT u.sigla FROM Unidade u
            WHERE u.codigo IN :codigos
            AND u.situacao = sgc.organizacao.model.SituacaoUnidade.ATIVA
            """)
    List<String> buscarSiglasPorCodigos(@Param("codigos") List<Long> codigos);

    @Query("""
            SELECT new sgc.organizacao.model.UnidadeResumoLeitura(
                u.codigo,
                u.nome,
                u.sigla,
                u.tipo
            )
            FROM Unidade u
            WHERE u.codigo IN :codigos
            AND u.situacao = sgc.organizacao.model.SituacaoUnidade.ATIVA
            """)
    List<UnidadeResumoLeitura> listarResumosPorCodigos(@Param("codigos") List<Long> codigos);

    @Query("""
            SELECT u.sigla FROM Unidade u
            WHERE u.codigo = :codigo
            AND u.situacao = sgc.organizacao.model.SituacaoUnidade.ATIVA
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
            WHERE u.situacao = sgc.organizacao.model.SituacaoUnidade.ATIVA
            """)
    List<UnidadeHierarquiaLeitura> listarEstruturasAtivas();

    @Query("""
            SELECT u FROM Unidade u
            LEFT JOIN FETCH u.unidadeSuperior
            LEFT JOIN FETCH u.titular
            LEFT JOIN FETCH u.responsabilidade
            LEFT JOIN FETCH u.responsabilidade.usuario
            WHERE u.codigo = :codigo
            AND u.situacao = sgc.organizacao.model.SituacaoUnidade.ATIVA
            """)
    Optional<Unidade> buscarPorCodigoComResponsavel(@Param("codigo") Long codigo);

    @Query("""
            SELECT u FROM Unidade u
            LEFT JOIN FETCH u.unidadeSuperior
            LEFT JOIN FETCH u.titular
            WHERE u.codigo = :codigo
            AND u.situacao = sgc.organizacao.model.SituacaoUnidade.ATIVA
            """)
    Optional<Unidade> buscarPorCodigoComSuperior(@Param("codigo") Long codigo);

    @Query("""
            SELECT u FROM Unidade u
            LEFT JOIN FETCH u.unidadeSuperior
            LEFT JOIN FETCH u.titular
            LEFT JOIN FETCH u.responsabilidade
            LEFT JOIN FETCH u.responsabilidade.usuario
            WHERE UPPER(u.sigla) = UPPER(:sigla)
            AND u.situacao = sgc.organizacao.model.SituacaoUnidade.ATIVA
            """)
    Optional<Unidade> buscarPorSiglaComResponsavel(@Param("sigla") String sigla);

    @Query("""
            SELECT u FROM Unidade u
            LEFT JOIN FETCH u.unidadeSuperior
            LEFT JOIN FETCH u.titular
            WHERE UPPER(u.sigla) = UPPER(:sigla)
            AND u.situacao = sgc.organizacao.model.SituacaoUnidade.ATIVA
            """)
    Optional<Unidade> buscarPorSiglaComSuperior(@Param("sigla") String sigla);

    @Query("""
            SELECT u.codigo
            FROM Unidade u
            WHERE u.situacao = sgc.organizacao.model.SituacaoUnidade.ATIVA
              AND u.codigo NOT IN (
                  SELECT um.unidadeCodigo
                  FROM UnidadeMapa um
                  JOIN um.mapaVigente mapa
                  JOIN mapa.subprocesso subprocesso
              )
              AND u.codigo NOT IN (
                  SELECT up.codigo.unidadeCodigo
                  FROM UnidadeProcesso up
                  WHERE up.processo.situacao IN (sgc.processo.model.SituacaoProcesso.CRIADO, sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO)
              )
            """)
    List<Long> buscarCodigosUnidadesSemMapaVigente();

    List<Unidade> findByUnidadeSuperiorCodigoAndSituacao(Long unidadeSuperiorCodigo, SituacaoUnidade situacao);

    default List<Unidade> findByUnidadeSuperiorCodigo(Long unidadeSuperiorCodigo) {
        return findByUnidadeSuperiorCodigoAndSituacao(unidadeSuperiorCodigo, SituacaoUnidade.ATIVA);
    }

    List<Unidade> findByTituloTitularAndSituacao(String tituloTitular, SituacaoUnidade situacao);

    default List<Unidade> findByTituloTitular(String tituloTitular) {
        return findByTituloTitularAndSituacao(tituloTitular, SituacaoUnidade.ATIVA);
    }
}

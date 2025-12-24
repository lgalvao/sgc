package sgc.processo.internal.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessoRepo extends JpaRepository<Processo, Long> {
    /**
     * Busca processos por situação.
     *
     * @param situacao Situação do processo (CRIADO, EM_ANDAMENTO, FINALIZADO)
     * @return Lista de processos com a situação especificada
     */
    @Query("SELECT DISTINCT p FROM Processo p LEFT JOIN FETCH p.participantes WHERE p.situacao = :situacao")
    List<Processo> findBySituacao(@Param("situacao") SituacaoProcesso situacao);

    /**
     * Busca processo por ID com participantes e suas unidades superiores inicializadas.
     * Otimizado para evitar N+1 ao construir hierarquia.
     */
    @Query("SELECT DISTINCT p FROM Processo p " +
            "LEFT JOIN FETCH p.participantes u " +
            "LEFT JOIN FETCH u.unidadeSuperior " +
            "WHERE p.codigo = :codigo")
    Optional<Processo> findByIdWithParticipantes(@Param("codigo") Long codigo);

    Page<Processo> findDistinctByParticipantes_CodigoIn(List<Long> codigos, Pageable pageable);

    Page<Processo> findDistinctByParticipantes_CodigoInAndSituacaoNot(
            List<Long> codigos, SituacaoProcesso situacao, Pageable pageable);

    @Query("SELECT distinct u.codigo FROM Processo p JOIN p.participantes u "
            + "WHERE p.situacao = :situacao AND u.codigo IN :codigos")
    List<Long> findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(
            @Param("situacao") SituacaoProcesso situacao,
            @Param("codigos") List<Long> codigos);

    @Query("SELECT distinct u.codigo FROM Processo p JOIN p.participantes u "
            + "WHERE p.situacao = :situacao AND p.tipo = :tipo")
    List<Long> findUnidadeCodigosBySituacaoAndTipo(
            @Param("situacao") SituacaoProcesso situacao,
            @Param("tipo") TipoProcesso tipo);

    @Query("SELECT distinct u.codigo FROM Processo p JOIN p.participantes u "
            + "WHERE p.situacao IN :situacoes AND (:idIgnorado IS NULL OR p.codigo <> :idIgnorado)")
    List<Long> findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
            @Param("situacoes") List<SituacaoProcesso> situacoes,
            @Param("idIgnorado") Long idIgnorado);
}

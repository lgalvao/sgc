package sgc.processo.model;

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

    @Query("""
            SELECT DISTINCT p FROM Processo p LEFT JOIN FETCH p.participantes WHERE p.situacao = :situacao
            """)
    List<Processo> listarPorSituacao(@Param("situacao") SituacaoProcesso situacao);

    @Query("""
            SELECT DISTINCT p FROM Processo p LEFT JOIN FETCH p.participantes
            WHERE p.situacao = :situacao
            ORDER BY p.dataFinalizacao DESC
            """)
    List<Processo> listarPorSituacaoComParticipantes(@Param("situacao") SituacaoProcesso situacao);

    @Query("""
            SELECT DISTINCT p FROM Processo p LEFT JOIN FETCH p.participantes
            WHERE p.situacao = :situacao
            AND p.codigo IN (
                SELECT p2.codigo FROM Processo p2 JOIN p2.participantes up2
                WHERE up2.codigo.unidadeCodigo IN :codigos
            )
            ORDER BY p.dataFinalizacao DESC
            """)
    List<Processo> listarPorSituacaoEUnidadeCodigos(
            @Param("situacao") SituacaoProcesso situacao,
            @Param("codigos") List<Long> codigos);

    @Query("""
            SELECT DISTINCT p FROM Processo p LEFT JOIN FETCH p.participantes
            WHERE p.situacao = :situacaoFinalizado
            AND p.dataFinalizacao IS NOT NULL
            AND p.dataFinalizacao < :corteInatividade
            ORDER BY p.dataFinalizacao DESC
            """)
    List<Processo> listarFinalizadosInativosComParticipantes(
            @Param("situacaoFinalizado") SituacaoProcesso situacaoFinalizado,
            @Param("corteInatividade") java.time.LocalDateTime corteInatividade);

    @Query("""
            SELECT DISTINCT p FROM Processo p LEFT JOIN FETCH p.participantes
            WHERE p.situacao = :situacaoFinalizado
            AND p.dataFinalizacao IS NOT NULL
            AND p.dataFinalizacao < :corteInatividade
            AND p.codigo IN (
                SELECT p2.codigo FROM Processo p2 JOIN p2.participantes up2
                WHERE up2.codigo.unidadeCodigo IN :codigos
            )
            ORDER BY p.dataFinalizacao DESC
            """)
    List<Processo> listarFinalizadosInativosEUnidadeCodigos(
            @Param("situacaoFinalizado") SituacaoProcesso situacaoFinalizado,
            @Param("corteInatividade") java.time.LocalDateTime corteInatividade,
            @Param("codigos") List<Long> codigos);

    /**
     * Busca processos onde as unidades participantes incluem as unidades especificadas
     * e o processo não está na situação especificada.
     */
    @Query("""
            SELECT DISTINCT p FROM Processo p
            JOIN p.participantes up
            WHERE up.codigo.unidadeCodigo IN :codigos
            AND p.situacao <> :situacao
            """)
    Page<Processo> listarPorParticipantesESituacaoDiferente(
            @Param("codigos") List<Long> codigos,
            @Param("situacao") SituacaoProcesso situacao,
            Pageable pageable);

    @Query("""
            SELECT p.codigo FROM Processo p
            ORDER BY p.dataCriacao DESC, p.codigo DESC
            """)
    Page<Long> listarCodigos(Pageable pageable);

    @Query("""
            SELECT p.codigo FROM Processo p
            WHERE p.situacao <> :situacaoFinalizado
               OR p.dataFinalizacao IS NULL
               OR p.dataFinalizacao >= :corteInatividade
            ORDER BY p.dataCriacao DESC, p.codigo DESC
            """)
    Page<Long> listarCodigosAtivos(
            @Param("situacaoFinalizado") SituacaoProcesso situacaoFinalizado,
            @Param("corteInatividade") java.time.LocalDateTime corteInatividade,
            Pageable pageable);

    @Query("""
            SELECT p.codigo FROM Processo p
            JOIN p.participantes up
            WHERE up.codigo.unidadeCodigo IN :codigos
            AND p.situacao <> :situacao
            GROUP BY p.codigo, p.dataCriacao
            """)
    Page<Long> listarCodigosPorParticipantesESituacaoDiferente(
            @Param("codigos") List<Long> codigos,
            @Param("situacao") SituacaoProcesso situacao,
            Pageable pageable);

    @Query("""
            SELECT p.codigo FROM Processo p
            WHERE p.situacao <> :situacao
            AND EXISTS (
                SELECT 1 FROM Subprocesso s
                WHERE s.processo = p
                AND s.unidade.codigo IN :codigos
            )
            """)
    Page<Long> listarCodigosPorSubprocessosESituacaoDiferente(
            @Param("codigos") List<Long> codigos,
            @Param("situacao") SituacaoProcesso situacao,
            Pageable pageable);

    @Query("""
            SELECT p.codigo FROM Processo p
            WHERE p.situacao <> :situacaoCriado
            AND (
                p.situacao <> :situacaoFinalizado
                OR p.dataFinalizacao IS NULL
                OR p.dataFinalizacao >= :corteInatividade
            )
            AND EXISTS (
                SELECT 1 FROM Subprocesso s
                WHERE s.processo = p
                AND s.unidade.codigo IN :codigos
            )
            """)
    Page<Long> listarCodigosAtivosPorSubprocessos(
            @Param("codigos") List<Long> codigos,
            @Param("situacaoCriado") SituacaoProcesso situacaoCriado,
            @Param("situacaoFinalizado") SituacaoProcesso situacaoFinalizado,
            @Param("corteInatividade") java.time.LocalDateTime corteInatividade,
            Pageable pageable);

    @Query("""
             SELECT DISTINCT p FROM Processo p
             LEFT JOIN FETCH p.participantes
            WHERE p.codigo IN :codigos
            """)
    List<Processo> listarPorCodigosComParticipantes(@Param("codigos") List<Long> codigos);

    @Query("""
            SELECT distinct u.codigo.unidadeCodigo FROM Processo p JOIN p.participantes u
            WHERE p.situacao = :situacao AND u.codigo.unidadeCodigo IN :codigos
            """)
    List<Long> listarUnidadesEmProcessoAtivo(
            @Param("situacao") SituacaoProcesso situacao,
            @Param("codigos") List<Long> codigos);

    @Query("""
            SELECT distinct u.codigo.unidadeCodigo FROM Processo p JOIN p.participantes u
            WHERE p.situacao = :situacao AND p.tipo = :tipo
            """)
    List<Long> listarUnidadesBloqueadasPorSituacaoETipo(
            @Param("situacao") SituacaoProcesso situacao,
            @Param("tipo") TipoProcesso tipo);

    @Query("""
            SELECT distinct u.codigo.unidadeCodigo FROM Processo p JOIN p.participantes u
            WHERE p.situacao IN :situacoes AND (:codigoIgnorado IS NULL OR p.codigo <> :codigoIgnorado)
            """)
    List<Long> listarUnidadesEmSituacoesExcetoProcesso(
            @Param("situacoes") List<SituacaoProcesso> situacoes,
            @Param("codigoIgnorado") Long codigoIgnorado);

    @Query("SELECT p FROM Processo p LEFT JOIN FETCH p.participantes WHERE p.codigo = :codigo")
    Optional<Processo> buscarPorCodigoComParticipantes(@Param("codigo") Long codigo);
}

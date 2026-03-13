package sgc.processo.model;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

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
            SELECT DISTINCT p FROM Processo p JOIN p.participantes up
            WHERE p.situacao = :situacao
            AND up.codigo.unidadeCodigo IN :codigos
            ORDER BY p.dataFinalizacao DESC
            """)
    List<Processo> listarPorSituacaoEUnidadeCodigos(
            @Param("situacao") SituacaoProcesso situacao,
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

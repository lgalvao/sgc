package sgc.diagnostico.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface AvaliacaoServidorRepo extends JpaRepository<AvaliacaoServidor, Long> {

    boolean existsByDiagnosticoCodigo(long diagnosticoCodigo);

    @Query("""
        SELECT a FROM AvaliacaoServidor a
        JOIN FETCH a.servidor
        JOIN FETCH a.competencia
        WHERE a.diagnostico.codigo = :diagnosticoCodigo
    """)
    List<AvaliacaoServidor> listarPorDiagnostico(@Param("diagnosticoCodigo") long diagnosticoCodigo);

    @Query("""
        SELECT a FROM AvaliacaoServidor a
        JOIN FETCH a.competencia
        WHERE a.diagnostico.codigo = :diagnosticoCodigo
        AND a.servidor.tituloEleitoral = :servidorTitulo
    """)
    List<AvaliacaoServidor> buscarAvaliacoesDoServidor(@Param("diagnosticoCodigo") long diagnosticoCodigo,
                                                        @Param("servidorTitulo") String servidorTitulo);

    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM AvaliacaoServidor a
        WHERE a.diagnostico.codigo = :diagnosticoCodigo
        AND a.situacaoServidor NOT IN :situacoesConcluidas
    """)
    boolean existsAvaliacaoPendentePorDiagnostico(
            @Param("diagnosticoCodigo") long diagnosticoCodigo,
            @Param("situacoesConcluidas") Collection<SituacaoAvaliacaoServidor> situacoesConcluidas
    );

    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM AvaliacaoServidor a
        WHERE a.diagnostico.codigo = :diagnosticoCodigo
        AND a.situacaoServidor = :situacaoAprovada
        AND NOT EXISTS (
            SELECT 1
            FROM SituacaoCapacitacao s
            WHERE s.diagnostico.codigo = a.diagnostico.codigo
            AND s.servidor.tituloEleitoral = a.servidor.tituloEleitoral
            AND s.competencia.codigo = a.competencia.codigo
            AND s.situacaoCapacitacao IS NOT NULL
        )
    """)
    boolean existsAvaliacaoAprovadaSemSituacaoCapacitacao(
            @Param("diagnosticoCodigo") long diagnosticoCodigo,
            @Param("situacaoAprovada") SituacaoAvaliacaoServidor situacaoAprovada
    );
}

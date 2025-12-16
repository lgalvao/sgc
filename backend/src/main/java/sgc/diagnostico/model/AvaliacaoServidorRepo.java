package sgc.diagnostico.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade AvaliacaoServidor.
 */
@Repository
public interface AvaliacaoServidorRepo extends JpaRepository<AvaliacaoServidor, Long> {
    /**
     * Busca todas as avaliações de um diagnóstico.
     */
    List<AvaliacaoServidor> findByDiagnosticoCodigo(Long diagnosticoCodigo);

    /**
     * Busca avaliações de um servidor específico em um diagnóstico.
     */
    List<AvaliacaoServidor> findByDiagnosticoCodigoAndServidorTituloEleitoral(
            Long diagnosticoCodigo, String servidorTitulo);

    /**
     * Busca avaliação específica de um servidor para uma competência.
     */
    Optional<AvaliacaoServidor> findByDiagnosticoCodigoAndServidorTituloEleitoralAndCompetenciaCodigo(
            Long diagnosticoCodigo, String servidorTitulo, Long competenciaCodigo);

    /**
     * Conta quantos servidores concluíram a autoavaliação.
     */
    @Query("SELECT COUNT(DISTINCT a.servidor) FROM AvaliacaoServidor a " +
            "WHERE a.diagnostico.codigo = :diagnosticoCodigo " +
            "AND a.situacao = 'AUTOAVALIACAO_CONCLUIDA'")
    long countServidoresComAvaliacaoCompleta(Long diagnosticoCodigo);

    /**
     * Conta total de servidores que têm avaliações no diagnóstico.
     */
    @Query("SELECT COUNT(DISTINCT a.servidor) FROM AvaliacaoServidor a " +
            "WHERE a.diagnostico.codigo = :diagnosticoCodigo")
    long countTotalServidores(Long diagnosticoCodigo);
}

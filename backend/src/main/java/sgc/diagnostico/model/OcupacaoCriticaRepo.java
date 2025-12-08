package sgc.diagnostico.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade OcupacaoCritica.
 */
@Repository
public interface OcupacaoCriticaRepo extends JpaRepository<OcupacaoCritica, Long> {

    /**
     * Busca todas as ocupações críticas de um diagnóstico.
     */
    List<OcupacaoCritica> findByDiagnosticoCodigo(Long diagnosticoCodigo);

    /**
     * Busca ocupação crítica específica de um servidor para uma competência.
     */
    Optional<OcupacaoCritica> findByDiagnosticoCodigoAndServidorTituloEleitoralAndCompetenciaCodigo(
            Long diagnosticoCodigo, String servidorTitulo, Long competenciaCodigo);
}

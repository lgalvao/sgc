package sgc.diagnostico.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SituacaoCapacitacaoRepo extends JpaRepository<SituacaoCapacitacao, Long> {
    @Query("SELECT o FROM SituacaoCapacitacao o WHERE o.diagnostico.codigo = :diagnosticoCodigo")
    List<SituacaoCapacitacao> listarPorDiagnostico(@Param("diagnosticoCodigo") Long diagnosticoCodigo);
}

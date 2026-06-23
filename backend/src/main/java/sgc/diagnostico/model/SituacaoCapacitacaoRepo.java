package sgc.diagnostico.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;

import java.util.*;

public interface SituacaoCapacitacaoRepo extends JpaRepository<SituacaoCapacitacao, Long> {
    @Query("SELECT o FROM SituacaoCapacitacao o WHERE o.diagnostico.codigo = :diagnosticoCodigo")
    List<SituacaoCapacitacao> listarPorDiagnostico(@Param("diagnosticoCodigo") Long diagnosticoCodigo);
}

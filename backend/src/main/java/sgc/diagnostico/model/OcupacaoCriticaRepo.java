package sgc.diagnostico.model;


import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface OcupacaoCriticaRepo extends JpaRepository<OcupacaoCritica, Long> {

    @Query("""
        SELECT o FROM OcupacaoCritica o 
        JOIN FETCH o.servidor 
        JOIN FETCH o.competencia 
        WHERE o.diagnostico.codigo = :diagnosticoCodigo
    """)
    List<OcupacaoCritica> listarPorDiagnostico(@Param("diagnosticoCodigo") Long diagnosticoCodigo);
}

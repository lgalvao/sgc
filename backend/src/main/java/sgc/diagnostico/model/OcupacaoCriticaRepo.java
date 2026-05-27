package sgc.diagnostico.model;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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

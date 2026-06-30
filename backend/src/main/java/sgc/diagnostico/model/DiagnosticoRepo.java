package sgc.diagnostico.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface DiagnosticoRepo extends JpaRepository<Diagnostico, Long> {

    @Query("""
                    SELECT d from Diagnostico d
                    LEFT JOIN FETCH d.avaliacaoServidores
                    LEFT JOIN FETCH d.situacaoCapacitacoes
                    WHERE d.subprocesso.codigo = :subprocessoCodigo
            """)
    Optional<Diagnostico> buscarPorSubprocessoComRelacionamentos(@Param("subprocessoCodigo") Long subprocessoCodigo);

    Optional<Diagnostico> findBySubprocessoCodigo(Long subprocessoCodigo);

}

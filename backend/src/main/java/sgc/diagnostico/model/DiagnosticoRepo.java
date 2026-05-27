package sgc.diagnostico.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiagnosticoRepo extends JpaRepository<Diagnostico, Integer> {

    @Query("""
            SELECT d from Diagnostico d 
            LEFT JOIN FETCH d.avaliaoServidores
            LEFT JOIN FETCH d.ocupacaoCriticas
            WHERE d.subprocesso.codigo = :subprocessoCodigo     
    """)
    Optional<Diagnostico> buscarPorSubprocessoComRelacionamentos(@Param("subprocessoCodigo") Long subprocessoCodigo);
    Optional<Diagnostico> findBySubprocessoCodigo(Long subprocessoCodigo);

}

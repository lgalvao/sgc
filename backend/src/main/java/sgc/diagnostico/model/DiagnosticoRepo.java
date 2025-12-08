package sgc.diagnostico.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade Diagnostico.
 */
@Repository
public interface DiagnosticoRepo extends JpaRepository<Diagnostico, Long> {

    /**
     * Busca diagnóstico por código do subprocesso.
     */
    Optional<Diagnostico> findBySubprocessoCodigo(Long subprocessoCodigo);

    /**
     * Busca todos os diagnósticos de um processo.
     */
    @Query("SELECT d FROM Diagnostico d WHERE d.subprocesso.processo.codigo = :processoCodigo")
    List<Diagnostico> findByProcessoCodigo(Long processoCodigo);
}

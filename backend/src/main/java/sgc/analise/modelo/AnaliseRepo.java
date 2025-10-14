package sgc.analise.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnaliseRepo extends JpaRepository<Analise, Long> {
    void deleteBySubprocessoCodigo(Long subprocessoCodigo);

    List<Analise> findBySubprocessoCodigoOrderByDataHoraDesc(Long subprocessoCodigo);

    List<Analise> findBySubprocesso_Codigo(Long subprocessoCodigo);
}

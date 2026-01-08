package sgc.analise.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnaliseRepo extends JpaRepository<Analise, Long> {
    List<Analise> findBySubprocessoCodigoOrderByDataHoraDesc(Long codSubprocesso);
    List<Analise> findBySubprocessoCodigo(Long codSubprocesso);
}

package sgc.subprocesso.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface AnaliseRepo extends JpaRepository<Analise, Long> {
    List<Analise> findBySubprocessoCodigoOrderByDataHoraDesc(Long codSubprocesso);

    List<Analise> findBySubprocessoCodigo(Long codSubprocesso);
}

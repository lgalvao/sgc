package sgc.organizacao.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

@Repository
public interface AtribuicaoTemporariaRepo extends JpaRepository<AtribuicaoTemporaria, Long> {
}

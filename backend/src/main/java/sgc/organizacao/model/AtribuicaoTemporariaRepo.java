package sgc.organizacao.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface AtribuicaoTemporariaRepo extends JpaRepository<AtribuicaoTemporaria, Long> {
    @Query("""
            SELECT a FROM AtribuicaoTemporaria a
            JOIN FETCH a.unidade
            """)
    List<AtribuicaoTemporaria> listarTodasComUnidade();
}

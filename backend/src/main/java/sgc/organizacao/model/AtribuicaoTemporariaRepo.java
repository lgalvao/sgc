package sgc.organizacao.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtribuicaoTemporariaRepo extends JpaRepository<AtribuicaoTemporaria, Long> {
}

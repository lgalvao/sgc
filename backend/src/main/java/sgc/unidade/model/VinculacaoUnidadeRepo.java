package sgc.unidade.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VinculacaoUnidadeRepo extends JpaRepository<VinculacaoUnidade, Long> {}

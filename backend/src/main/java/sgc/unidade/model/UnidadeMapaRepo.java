package sgc.unidade.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnidadeMapaRepo extends JpaRepository<UnidadeMapa, Long> {}

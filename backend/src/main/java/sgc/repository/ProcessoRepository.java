package sgc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sgc.model.Processo;

@Repository
public interface ProcessoRepository extends JpaRepository<Processo, Long> {
}
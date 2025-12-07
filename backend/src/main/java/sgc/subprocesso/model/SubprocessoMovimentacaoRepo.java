package sgc.subprocesso.model;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubprocessoMovimentacaoRepo extends JpaRepository<Movimentacao, Long> {
    List<Movimentacao> findBySubprocessoCodigoOrderByDataHoraDesc(Long subprocessoCodigo);
}

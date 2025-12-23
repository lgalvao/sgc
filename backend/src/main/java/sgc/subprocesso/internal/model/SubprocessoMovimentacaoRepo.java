package sgc.subprocesso.internal.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubprocessoMovimentacaoRepo extends JpaRepository<Movimentacao, Long> {
    List<Movimentacao> findBySubprocessoCodigoOrderByDataHoraDesc(Long subprocessoCodigo);
}

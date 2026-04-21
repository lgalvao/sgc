package sgc.alerta.model;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.time.*;
import java.util.*;

@Repository
public interface NotificacaoEmailRepo extends JpaRepository<NotificacaoEmail, Long> {
    boolean existsByChaveIdempotencia(String chaveIdempotencia);

    Optional<NotificacaoEmail> findByChaveIdempotencia(String chaveIdempotencia);

    List<NotificacaoEmail> findBySituacaoInAndProximaTentativaEmLessThanEqualOrderByDataHoraCriacaoAsc(
            Collection<SituacaoNotificacaoEmail> situacoes,
            LocalDateTime agora,
            Pageable pageable
    );
}

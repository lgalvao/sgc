package sgc.alerta.model;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update NotificacaoEmail notificacao
               set notificacao.situacao = sgc.alerta.model.SituacaoNotificacaoEmail.ENVIANDO
             where notificacao.codigo = :codigo
               and notificacao.situacao in (
                    sgc.alerta.model.SituacaoNotificacaoEmail.PENDENTE,
                    sgc.alerta.model.SituacaoNotificacaoEmail.FALHA_TEMPORARIA
               )
               and notificacao.proximaTentativaEm <= :agora
            """)
    int marcarEnviandoSeDisponivel(@Param("codigo") Long codigo, @Param("agora") LocalDateTime agora);

    List<NotificacaoEmail> findBySubprocesso_CodigoOrderByDataHoraCriacaoDesc(Long subprocessoCodigo, Pageable pageable);
}

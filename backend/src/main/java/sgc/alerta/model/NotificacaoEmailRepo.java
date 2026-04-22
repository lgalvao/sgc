package sgc.alerta.model;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;
import sgc.alerta.dto.*;

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

    @Query("""
            select new sgc.alerta.dto.NotificacaoSubprocessoResumoQuery(
                subprocesso.codigo,
                processo.codigo,
                processo.descricao,
                unidade.sigla,
                subprocesso.situacao,
                count(notificacao.codigo),
                coalesce(sum(case when notificacao.situacao = sgc.alerta.model.SituacaoNotificacaoEmail.PENDENTE then 1 else 0 end), 0),
                coalesce(sum(case when notificacao.situacao = sgc.alerta.model.SituacaoNotificacaoEmail.ENVIANDO then 1 else 0 end), 0),
                coalesce(sum(case when notificacao.situacao = sgc.alerta.model.SituacaoNotificacaoEmail.ENVIADO then 1 else 0 end), 0),
                coalesce(sum(case when notificacao.situacao = sgc.alerta.model.SituacaoNotificacaoEmail.FALHA_TEMPORARIA then 1 else 0 end), 0),
                coalesce(sum(case when notificacao.situacao = sgc.alerta.model.SituacaoNotificacaoEmail.FALHA_DEFINITIVA then 1 else 0 end), 0),
                max(notificacao.dataHoraCriacao),
                min(case
                    when notificacao.situacao in (
                        sgc.alerta.model.SituacaoNotificacaoEmail.PENDENTE,
                        sgc.alerta.model.SituacaoNotificacaoEmail.FALHA_TEMPORARIA
                    )
                    then notificacao.proximaTentativaEm
                    else null
                end),
                coalesce(max(notificacao.tentativas), 0),
                max(notificacao.ultimoErro)
            )
              from Subprocesso subprocesso
              join subprocesso.processo processo
              join subprocesso.unidade unidade
              join NotificacaoEmail notificacao on notificacao.subprocesso = subprocesso
             where processo.situacao = sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO
             group by subprocesso.codigo, processo.codigo, processo.descricao, unidade.sigla, subprocesso.situacao
             order by processo.descricao asc, unidade.sigla asc
            """)
    List<NotificacaoSubprocessoResumoQuery> resumirPorSubprocessosDeProcessosAtivos();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update NotificacaoEmail notificacao
               set notificacao.situacao = sgc.alerta.model.SituacaoNotificacaoEmail.PENDENTE,
                   notificacao.tentativas = 0,
                   notificacao.proximaTentativaEm = :agora,
                   notificacao.dataHoraEnvio = null,
                   notificacao.ultimoErro = null
             where notificacao.subprocesso.codigo = :subprocessoCodigo
               and notificacao.situacao = sgc.alerta.model.SituacaoNotificacaoEmail.FALHA_DEFINITIVA
            """)
    int reenfileirarFalhasDefinitivasPorSubprocesso(
            @Param("subprocessoCodigo") Long subprocessoCodigo,
            @Param("agora") LocalDateTime agora
    );
}

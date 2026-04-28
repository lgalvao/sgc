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
            Collection<SituacaoNotificacao> situacoes,
            LocalDateTime agora,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update NotificacaoEmail notificacao
               set notificacao.situacao = sgc.alerta.model.SituacaoNotificacao.ENVIANDO
             where notificacao.codigo = :codigo
               and notificacao.situacao in (
                    sgc.alerta.model.SituacaoNotificacao.PENDENTE,
                    sgc.alerta.model.SituacaoNotificacao.FALHA_TEMPORARIA
               )
               and notificacao.proximaTentativaEm <= :agora
            """)
    int marcarEnviandoSeDisponivel(@Param("codigo") Long codigo, @Param("agora") LocalDateTime agora);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update NotificacaoEmail notificacao
               set notificacao.situacao = sgc.alerta.model.SituacaoNotificacao.ENVIADO,
                   notificacao.dataHoraEnvio = :#{#cmd.agora},
                   notificacao.ultimoErro = null
             where notificacao.codigo = :#{#cmd.codigo}
               and notificacao.situacao = sgc.alerta.model.SituacaoNotificacao.ENVIANDO
            """)
    int marcarEnviado(@Param("cmd") MarcarEnviadoCommand cmd);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update NotificacaoEmail notificacao
               set notificacao.situacao = :#{#cmd.situacao},
                   notificacao.tentativas = :#{#cmd.tentativas},
                   notificacao.ultimoErro = :#{#cmd.ultimoErro},
                   notificacao.proximaTentativaEm = :#{#cmd.proximaTentativaEm}
             where notificacao.codigo = :#{#cmd.codigo}
               and notificacao.situacao = sgc.alerta.model.SituacaoNotificacao.ENVIANDO
            """)
    int marcarFalha(@Param("cmd") MarcarFalhaCommand cmd);

    List<NotificacaoEmail> findBySubprocesso_CodigoOrderByDataHoraCriacaoDesc(Long subprocessoCodigo, Pageable pageable);

    @Query("""
            select notificacao
              from NotificacaoEmail notificacao
              left join fetch notificacao.subprocesso subprocesso
              left join fetch subprocesso.processo processo
              left join fetch subprocesso.unidade unidade
             where processo.situacao = sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO
             order by notificacao.dataHoraCriacao desc
            """)
    List<NotificacaoEmail> buscarRecentesDeProcessosEmAndamento(Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update NotificacaoEmail notificacao
               set notificacao.situacao = sgc.alerta.model.SituacaoNotificacao.PENDENTE,
                   notificacao.tentativas = 0,
                   notificacao.proximaTentativaEm = :agora,
                   notificacao.dataHoraEnvio = null,
                   notificacao.ultimoErro = null
             where notificacao.codigo = :codigo
               and notificacao.situacao = sgc.alerta.model.SituacaoNotificacao.FALHA_DEFINITIVA
            """)
    int reenviarPorCodigo(@Param("codigo") Long codigo, @Param("agora") LocalDateTime agora);

    @Query("""
            select new sgc.alerta.dto.NotificacaoSubprocessoResumoQuery(
                subprocesso.codigo,
                processo.codigo,
                processo.descricao,
                unidade.sigla,
                subprocesso.situacao,
                count(notificacao.codigo),
                coalesce(sum(case when notificacao.situacao = sgc.alerta.model.SituacaoNotificacao.PENDENTE then 1 else 0 end), 0),
                coalesce(sum(case when notificacao.situacao = sgc.alerta.model.SituacaoNotificacao.ENVIANDO then 1 else 0 end), 0),
                coalesce(sum(case when notificacao.situacao = sgc.alerta.model.SituacaoNotificacao.ENVIADO then 1 else 0 end), 0),
                coalesce(sum(case when notificacao.situacao = sgc.alerta.model.SituacaoNotificacao.FALHA_TEMPORARIA then 1 else 0 end), 0),
                coalesce(sum(case when notificacao.situacao = sgc.alerta.model.SituacaoNotificacao.FALHA_DEFINITIVA then 1 else 0 end), 0),
                max(notificacao.dataHoraCriacao),
                min(case
                    when notificacao.situacao in (
                        sgc.alerta.model.SituacaoNotificacao.PENDENTE,
                        sgc.alerta.model.SituacaoNotificacao.FALHA_TEMPORARIA
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
               set notificacao.situacao = sgc.alerta.model.SituacaoNotificacao.PENDENTE,
                   notificacao.tentativas = 0,
                   notificacao.proximaTentativaEm = :agora,
                   notificacao.dataHoraEnvio = null,
                   notificacao.ultimoErro = null
             where notificacao.subprocesso.codigo = :subprocessoCodigo
               and notificacao.situacao = sgc.alerta.model.SituacaoNotificacao.FALHA_DEFINITIVA
            """)
    int reenfileirarFalhasDefinitivasPorSubprocesso(
            @Param("subprocessoCodigo") Long subprocessoCodigo,
            @Param("agora") LocalDateTime agora
    );

    record MarcarEnviadoCommand(Long codigo, LocalDateTime agora) {
    }

    record MarcarFalhaCommand(
            Long codigo,
            SituacaoNotificacao situacao,
            int tentativas,
            String ultimoErro,
            LocalDateTime proximaTentativaEm
    ) {
    }
}

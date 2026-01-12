package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseService;
import sgc.analise.dto.CriarAnaliseReq;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.eventos.EventoTransicaoSubprocesso;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.*;

/**
 * Serviço consolidado para gerenciar transições e workflows de subprocessos.
 *
 * <p><b>Arquitetura Sprint 2:</b> Este serviço foi consolidado, absorbendo as
 * responsabilidades de {@code SubprocessoWorkflowExecutor} para eliminar duplicação
 * e melhorar a coesão arquitetural.
 *
 * <p><b>Responsabilidades:</b>
 * <ol>
 *   <li>Salvar movimentações (trilha de auditoria)</li>
 *   <li>Publicar eventos de transição (comunicação assíncrona)</li>
 *   <li>Executar workflows completos com análises</li>
 *   <li>Atualizar estados de subprocessos</li>
 * </ol>
 *
 * <p>Este serviço <b>DEVE</b> ser chamado dentro de uma transação existente
 * ({@code @Transactional} no método chamador) para garantir atomicidade.
 *
 * <p><b>Nota arquitetural:</b> Mantido público temporariamente para compatibilidade com testes.
 * Uso deveria ser via {@link SubprocessoFacade}.
 *
 * @since 2.0.0 (consolidação arquitetural Sprint 2)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoTransicaoService {

    private final MovimentacaoRepo movimentacaoRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final SubprocessoRepo repositorioSubprocesso;
    private final AnaliseService analiseService;

    /**
     * Registra uma transição de subprocesso: salva movimentação e publica evento.
     *
     * @param subprocesso Subprocesso que está transitando
     * @param tipo Tipo da transição (define descrição e templates de comunicação)
     * @param origem Unidade de origem da transição
     * @param destino Unidade de destino da transição
     * @param usuario Usuário que executou a ação
     * @param observacoes Observações opcionais (ex: motivo de devolução)
     */
    public void registrar(
            Subprocesso subprocesso,
            TipoTransicao tipo,
            Unidade origem,
            Unidade destino,
            Usuario usuario,
            @Nullable String observacoes) {

        // 1. Salvar movimentação (atômico com a transação do chamador)
        Movimentacao movimentacao = new Movimentacao(
                subprocesso,
                origem,
                destino,
                tipo.getDescricaoMovimentacao(),
                usuario
        );
        movimentacaoRepo.save(movimentacao);

        // 2. Publicar evento para comunicação (alertas/emails)
        EventoTransicaoSubprocesso evento = EventoTransicaoSubprocesso.builder()
                .subprocesso(subprocesso)
                .tipo(tipo)
                .usuario(usuario)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .observacoes(observacoes)
                .build();

        eventPublisher.publishEvent(evento);
    }

    /**
     * Registra uma transição sem observações.
     *
     * @see #registrar(Subprocesso, TipoTransicao, Unidade, Unidade, Usuario, String)
     */
    public void registrar(
            Subprocesso subprocesso,
            TipoTransicao tipo,
            Unidade origem,
            Unidade destino,
            Usuario usuario) {
        registrar(subprocesso, tipo, origem, destino, usuario, null);
    }

    // ===== Execução de Workflow Completo (Consolidado de SubprocessoWorkflowExecutor) =====

    /**
     * Executa um workflow completo com análise e transição.
     *
     * <p><b>Consolidação Arquitetural:</b> Este método absorve a funcionalidade
     * do antigo {@code SubprocessoWorkflowExecutor.registrarAnaliseETransicao()},
     * eliminando duplicação e centralizando a lógica de workflow.
     *
     * <p>Sequência de execução:
     * <ol>
     *   <li>Cria análise do subprocesso</li>
     *   <li>Atualiza estado do subprocesso</li>
     *   <li>Registra transição com evento</li>
     * </ol>
     *
     * <p>Utilizado principalmente em workflows de aprovação/devolução onde
     * é necessário registrar análise + mudança de estado.
     *
     * @param sp                       O subprocesso alvo
     * @param novaSituacao             O novo estado do subprocesso
     * @param tipoTransicao            O tipo da transição
     * @param tipoAnalise              O tipo da análise
     * @param tipoAcaoAnalise          A ação da análise (aprovar, devolver, etc)
     * @param unidadeAnalise           A unidade que realizou a análise
     * @param unidadeOrigemTransicao   A unidade de origem da transição
     * @param unidadeDestinoTransicao  A unidade de destino da transição (pode ser null)
     * @param usuario                  O usuário que executou o workflow
     * @param observacoes              Observações (pode ser null)
     * @param motivoAnalise            Motivo da análise (pode ser null)
     *
     * @since 2.0.0 Consolidado de SubprocessoWorkflowExecutor
     */
    @Transactional
    public void registrarAnaliseETransicao(
            Subprocesso sp,
            SituacaoSubprocesso novaSituacao,
            TipoTransicao tipoTransicao,
            TipoAnalise tipoAnalise,
            TipoAcaoAnalise tipoAcaoAnalise,
            Unidade unidadeAnalise,
            Unidade unidadeOrigemTransicao,
            @Nullable Unidade unidadeDestinoTransicao,
            Usuario usuario,
            @Nullable String observacoes,
            @Nullable String motivoAnalise
    ) {
        // 1. Criar Análise
        analiseService.criarAnalise(
                sp,
                CriarAnaliseReq.builder()
                        .codSubprocesso(sp.getCodigo())
                        .observacoes(observacoes)
                        .tipo(tipoAnalise)
                        .acao(tipoAcaoAnalise)
                        .siglaUnidade(unidadeAnalise.getSigla())
                        .tituloUsuario(String.valueOf(usuario.getTituloEleitoral()))
                        .motivo(motivoAnalise)
                        .build());

        // 2. Atualizar Estado
        sp.setSituacao(novaSituacao);
        repositorioSubprocesso.save(sp);

        // 3. Registrar Transição
        registrar(
                sp,
                tipoTransicao,
                unidadeOrigemTransicao,
                unidadeDestinoTransicao,
                usuario,
                observacoes);

        log.info("Workflow executado: Subprocesso {} -> {}, Transição {}",
                sp.getCodigo(), novaSituacao, tipoTransicao);
    }
}

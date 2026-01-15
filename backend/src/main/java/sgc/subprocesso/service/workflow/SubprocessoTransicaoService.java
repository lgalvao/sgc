package sgc.subprocesso.service.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseService;
import sgc.analise.dto.CriarAnaliseCommand;
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
            @Nullable Unidade origem,
            @Nullable Unidade destino,
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
            @Nullable Unidade origem,
            @Nullable Unidade destino,
            Usuario usuario) {
        registrar(subprocesso, tipo, origem, destino, usuario, null);
    }

    // ===== Execução de Workflow Completo (Consolidado de SubprocessoWorkflowExecutor) =====

    /**
     * Parâmetros para registro de workflow completo.
     */
    public record RegistrarWorkflowReq(
            Subprocesso sp,
            SituacaoSubprocesso novaSituacao,
            TipoTransicao tipoTransicao,
            TipoAnalise tipoAnalise,
            TipoAcaoAnalise tipoAcaoAnalise,
            Unidade unidadeAnalise,
            @Nullable Unidade unidadeOrigemTransicao,
            @Nullable Unidade unidadeDestinoTransicao,
            Usuario usuario,
            @Nullable String observacoes,
            @Nullable String motivoAnalise
    ) {}

    /**
     * Executa um workflow completo com análise e transição.
     *
     * <p>Utilizado principalmente em workflows de aprovação/devolução onde
     * é necessário registrar análise + mudança de estado.
     *
     * @param req Objeto contendo todos os parâmetros da transição de workflow
     * @since 2.1.0 Refatorado para usar Record (limite de parâmetros)
     */
    @Transactional
    public void registrarAnaliseETransicao(RegistrarWorkflowReq req) {
        // 1. Criar Análise
        analiseService.criarAnalise(
                req.sp(),
                CriarAnaliseCommand.builder()
                        .codSubprocesso(req.sp().getCodigo())
                        .observacoes(req.observacoes())
                        .tipo(req.tipoAnalise())
                        .acao(req.tipoAcaoAnalise())
                        .siglaUnidade(req.unidadeAnalise().getSigla())
                        .tituloUsuario(req.usuario().getTituloEleitoral())
                        .motivo(req.motivoAnalise())
                        .build());

        // 2. Atualizar Estado
        req.sp().setSituacao(req.novaSituacao());
        repositorioSubprocesso.save(req.sp());

        // 3. Registrar Transição
        registrar(
                req.sp(),
                req.tipoTransicao(),
                req.unidadeOrigemTransicao(),
                req.unidadeDestinoTransicao(),
                req.usuario(),
                req.observacoes());

        log.info("Workflow executado: Subprocesso {} -> {}, Transição {}",
                req.sp().getCodigo(), req.novaSituacao(), req.tipoTransicao());
    }
}

package sgc.subprocesso.service.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseFacade;
import sgc.analise.dto.CriarAnaliseCommand;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.eventos.EventoTransicaoSubprocesso;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.MovimentacaoRepositoryService;
import sgc.subprocesso.service.SubprocessoRepositoryService;

/**
 * Serviço consolidado para gerenciar transições e workflows de subprocessos.
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
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoTransicaoService {
    private final MovimentacaoRepositoryService movimentacaoService;
    private final ApplicationEventPublisher eventPublisher;
    private final SubprocessoRepositoryService subprocessoService;
    private final AnaliseFacade analiseFacade;

    /**
     * Registra uma transição de subprocesso: salva movimentação e publica evento.
     *
     * @param subprocesso Subprocesso que está transitando
     * @param tipo        Tipo da transição (define descrição e templates de comunicação)
     * @param origem      Unidade de origem da transição
     * @param destino     Unidade de destino da transição
     * @param usuario     Usuário que executou a ação
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
        Movimentacao movimentacao = Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .descricao(tipo.getDescricaoMovimentacao())
                .usuario(usuario)
                .build();
        movimentacaoService.save(movimentacao);

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
     * <p>Utilizado principalmente em workflows de aprovação/devolução onde
     * é necessário registrar análise + mudança de estado.
     *
     * @param req Objeto contendo todos os parâmetros da transição de workflow
     */
    @Transactional
    public void registrarAnaliseETransicao(RegistrarWorkflowReq req) {
        // 1. Criar Análise
        analiseFacade.criarAnalise(
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
        subprocessoService.save(req.sp());

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

    /**
     * Parâmetros para registro de workflow completo.
     */
    public record RegistrarWorkflowReq(
            Subprocesso sp,
            @Nullable SituacaoSubprocesso novaSituacao,
            TipoTransicao tipoTransicao,
            TipoAnalise tipoAnalise,
            TipoAcaoAnalise tipoAcaoAnalise,
            @Nullable Unidade unidadeAnalise,
            @Nullable Unidade unidadeOrigemTransicao,
            @Nullable Unidade unidadeDestinoTransicao,
            Usuario usuario,
            @Nullable String observacoes,
            @Nullable String motivoAnalise
    ) {
    }
}

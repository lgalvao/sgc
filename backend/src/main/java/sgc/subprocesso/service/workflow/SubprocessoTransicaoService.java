package sgc.subprocesso.service.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseFacade;
import sgc.analise.dto.CriarAnaliseCommand;
import sgc.subprocesso.dto.RegistrarTransicaoCommand;
import sgc.subprocesso.dto.RegistrarWorkflowCommand;
import sgc.subprocesso.eventos.EventoTransicaoSubprocesso;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;

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
    private final MovimentacaoRepo movimentacaoRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final AnaliseFacade analiseFacade;
    private final UsuarioFacade usuarioFacade;

    /**
     * Registra uma transição de subprocesso (auditoria e eventos).
     *
     * @param cmd Comando contendo os dados da transição
     */
    @Transactional
    public void registrar(RegistrarTransicaoCommand cmd) {
        Usuario usuario = cmd.usuario() != null ? cmd.usuario() : usuarioFacade.obterUsuarioAutenticado();
        if (usuario == null) {
            throw new ErroProcessoEmSituacaoInvalida("Usuário não autenticado");
        }

        // 1. Salvar movimentação (trilha de auditoria completa)
        Movimentacao movimentacao = Movimentacao.builder()
                .subprocesso(cmd.sp())
                .unidadeOrigem(cmd.origem())
                .unidadeDestino(cmd.destino())
                .descricao(cmd.tipo().getDescricaoMovimentacao())
                .usuario(usuario)
                .build();
        movimentacaoRepo.save(movimentacao);

        // 2. Publicar evento para comunicação (alertas/emails)
        EventoTransicaoSubprocesso evento = EventoTransicaoSubprocesso.builder()
                .subprocesso(cmd.sp())
                .tipo(cmd.tipo())
                .usuario(usuario)
                .unidadeOrigem(cmd.origem())
                .unidadeDestino(cmd.destino())
                .observacoes(cmd.observacoes())
                .build();

        eventPublisher.publishEvent(evento);
    }

    /**
     * Executa um workflow completo com análise e transição.
     * 
     * @param cmd Comando contendo dados da análise e da transição
     */
    @Transactional
    public void registrarAnaliseETransicao(RegistrarWorkflowCommand cmd) {
        Subprocesso sp = cmd.sp();

        // 1. Criar Análise (Registro histórico da decisão técnica)
        analiseFacade.criarAnalise(
                sp,
                CriarAnaliseCommand.builder()
                        .codSubprocesso(sp.getCodigo())
                        .observacoes(cmd.observacoes())
                        .tipo(cmd.tipoAnalise())
                        .acao(cmd.tipoAcaoAnalise())
                        .siglaUnidade(cmd.unidadeAnalise().getSigla())
                        .tituloUsuario(cmd.usuario().getTituloEleitoral())
                        .motivo(cmd.motivoAnalise())
                        .build());

        // 2. Atualizar Estado
        sp.setSituacao(cmd.novaSituacao());

        // 3. Registrar Transição e Eventos
        registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(cmd.tipoTransicao())
                .origem(cmd.unidadeOrigemTransicao())
                .destino(cmd.unidadeDestinoTransicao())
                .usuario(cmd.usuario())
                .observacoes(cmd.observacoes())
                .build());

        log.info("Workflow executado: Subprocesso {} -> {}, Transição {}",
                sp.getCodigo(), cmd.novaSituacao(), cmd.tipoTransicao());
    }
}

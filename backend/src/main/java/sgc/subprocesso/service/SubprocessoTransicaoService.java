package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.AnaliseFacade;
import sgc.subprocesso.dto.CriarAnaliseCommand;
import sgc.subprocesso.dto.RegistrarTransicaoCommand;
import sgc.subprocesso.dto.RegistrarWorkflowCommand;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoTransicao;

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
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoTransicaoService {
    private final MovimentacaoRepo movimentacaoRepo;
    private final AlertaFacade alertaService;
    private final SubprocessoEmailService emailService;
    private final AnaliseFacade analiseFacade;
    private final UsuarioFacade usuarioFacade;

    /**
     * Registra uma transição de subprocesso (auditoria e eventos).
     *
     * @param cmd Comando contendo os dados da transição
     */
    @Transactional
    public void registrar(RegistrarTransicaoCommand cmd) {
        Usuario usuario = cmd.usuario() != null ? cmd.usuario() : usuarioFacade.usuarioAutenticado();

        // 1. Salvar movimentação (trilha de auditoria completa)
        Movimentacao movimentacao = Movimentacao.builder()
                .subprocesso(cmd.sp())
                .unidadeOrigem(cmd.origem())
                .unidadeDestino(cmd.destino())
                .descricao(cmd.tipo().getDescricaoMovimentacao())
                .usuario(usuario)
                .build();
        movimentacaoRepo.save(movimentacao);

        // Atualiza a localização atual no cache do objeto para evitar N+1 em testes e chamadas sequenciais
        cmd.sp().setLocalizacaoAtualCache(cmd.destino() != null ? cmd.destino() : cmd.sp().getUnidade());

        // 2. Notificar via alerta e e-mail
        notificarTransicao(cmd.sp(), cmd.tipo(), cmd.origem(), cmd.destino(), cmd.observacoes());
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

        // 2. Atualizar situacao
        sp.setSituacao(cmd.novaSituacao());

        // 3. Registrar Transição
        registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(cmd.tipoTransicao())
                .origem(cmd.unidadeOrigemTransicao())
                .destino(cmd.unidadeDestinoTransicao())
                .usuario(cmd.usuario())
                .observacoes(cmd.observacoes())
                .build());

        log.debug("{} -> {}", cmd.novaSituacao(), cmd.tipoTransicao());
    }

    private void notificarTransicao(Subprocesso sp, TipoTransicao tipo,
                                     Unidade origem,
                                     Unidade destino,
                                     String observacoes) {
        try {
            if (tipo.geraAlerta()) {
                String sigla = sp.getUnidade().getSigla();
                String descricao = tipo.formatarAlerta(sigla);
                alertaService.criarAlertaTransicao(sp.getProcesso(), descricao, origem, destino);
            }

            if (tipo.enviaEmail()) {
                emailService.notificarMovimentacao(sp, tipo, origem, destino, observacoes);
            }
        } catch (Exception e) {
            log.error("Falha ao enviar notificação de transição {}: {}", tipo, e.getMessage(), e);
        }
    }
}

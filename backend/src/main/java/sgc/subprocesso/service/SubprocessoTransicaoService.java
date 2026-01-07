package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.eventos.EventoTransicaoSubprocesso;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;

/**
 * Serviço responsável por registrar transições de subprocesso.
 *
 * <p>Encapsula duas operações atômicas que devem ocorrer em toda transição:
 * <ol>
 *   <li>Salvar a movimentação (trilha de auditoria)</li>
 *   <li>Publicar evento para comunicação (alertas e e-mails)</li>
 * </ol>
 *
 * <p>Este serviço <b>DEVE</b> ser chamado dentro de uma transação existente
 * ({@code @Transactional} no método chamador). A movimentação é salva na
 * mesma transação que a mudança de estado do subprocesso, garantindo atomicidade.
 *
 * <p>Exemplo de uso:
 * <pre>{@code
 * @Transactional
 * public void disponibilizarCadastro(Long codSubprocesso, Usuario usuario) {
 *     Subprocesso sp = buscarSubprocesso(codSubprocesso);
 *     sp.setSituacao(CADASTRO_DISPONIBILIZADO);
 *     subprocessoRepo.save(sp);
 *
 *     transicaoService.registrar(
 *         sp,
 *         TipoTransicao.CADASTRO_DISPONIBILIZADO,
 *         sp.getUnidade(),
 *         sp.getUnidade().getUnidadeSuperior(),
 *         usuario
 *     );
 * }
 * }</pre>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoTransicaoService {

    private final MovimentacaoRepo movimentacaoRepo;
    private final ApplicationEventPublisher eventPublisher;

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
            String observacoes) {

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
}

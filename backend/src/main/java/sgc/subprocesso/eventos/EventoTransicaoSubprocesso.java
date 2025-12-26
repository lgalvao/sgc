package sgc.subprocesso.eventos;

import lombok.Builder;
import lombok.Data;
import sgc.subprocesso.model.Subprocesso;
import sgc.unidade.model.Unidade;
import sgc.usuario.model.Usuario;

/**
 * Evento de domínio publicado quando ocorre uma transição de estado em um subprocesso.
 *
 * <p>Este evento único substitui as 19+ classes de evento anteriores (EventoSubprocessoCadastroDisponibilizado,
 * EventoSubprocessoCadastroDevolvido, etc.), centralizando a comunicação de transições.
 *
 * <p>O campo {@link #tipo} determina qual transição ocorreu e contém os metadados para:
 * <ul>
 *   <li>Descrição da movimentação (trilha de auditoria)</li>
 *   <li>Template do alerta interno</li>
 *   <li>Template do e-mail de notificação</li>
 * </ul>
 *
 * <p>O listener {@code SubprocessoComunicacaoListener} processa este evento para criar
 * alertas e enviar e-mails conforme o tipo de transição.
 */
@Data
@Builder
public class EventoTransicaoSubprocesso {

    /**
     * O subprocesso que transitou.
     */
    private Subprocesso subprocesso;

    /**
     * Tipo da transição, determinando comportamentos de comunicação.
     */
    private TipoTransicao tipo;

    /**
     * Usuário que executou a ação que causou a transição.
     */
    private Usuario usuario;

    /**
     * Unidade de origem da transição (de onde o subprocesso está saindo).
     */
    private Unidade unidadeOrigem;

    /**
     * Unidade de destino da transição (para onde o subprocesso está indo).
     */
    private Unidade unidadeDestino;

    /**
     * Observações opcionais associadas à transição.
     * <p>Usado para: motivo de devolução, sugestões, justificativas, etc.
     */
    private String observacoes;
}


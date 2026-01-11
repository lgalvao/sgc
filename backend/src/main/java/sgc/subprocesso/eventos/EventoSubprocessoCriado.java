package sgc.subprocesso.eventos;

import lombok.Builder;
import lombok.Data;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;

/**
 * Evento de domínio publicado quando um subprocesso é criado.
 *
 * <p>Este evento é disparado após a criação bem-sucedida de um subprocesso,
 * permitindo que outros módulos inicializem dados relacionados ou preparem
 * workflows.
 *
 * <p><b>Casos de uso:</b>
 * <ul>
 *   <li>Inicialização de mapa de competências vazio</li>
 *   <li>Criação de alertas de início de mapeamento</li>
 *   <li>Notificação de titulares de unidades</li>
 *   <li>Preparação de templates de cadastro</li>
 *   <li>Auditoria de criação</li>
 * </ul>
 *
 * <p><b>Exemplo de uso:</b>
 * <pre>{@code
 * EventoSubprocessoCriado evento = EventoSubprocessoCriado.builder()
 *     .subprocesso(subprocessoCriado)
 *     .usuario(usuarioAutenticado)
 *     .dataHoraCriacao(LocalDateTime.now())
 *     .criadoPorProcesso(true)
 *     .build();
 * eventPublisher.publishEvent(evento);
 * }</pre>
 *
 * @see EventoSubprocessoAtualizado
 * @see EventoSubprocessoExcluido
 * @see EventoTransicaoSubprocesso
 */
@Data
@Builder
public class EventoSubprocessoCriado {

    /**
     * O subprocesso que foi criado.
     */
    private Subprocesso subprocesso;

    /**
     * Usuário que criou o subprocesso.
     * <p>Pode ser null se criado automaticamente pelo sistema (ex: ao iniciar processo).
     */
    private @org.jspecify.annotations.Nullable Usuario usuario;

    /**
     * Data e hora da criação.
     */
    private LocalDateTime dataHoraCriacao;

    /**
     * Indica se o subprocesso foi criado automaticamente ao iniciar um processo.
     * <p>Se true, a criação faz parte de um lote de subprocessos (um por unidade participante).
     */
    private boolean criadoPorProcesso;

    /**
     * Código do processo pai.
     */
    private @org.jspecify.annotations.Nullable Long codProcesso;

    /**
     * Código da unidade responsável pelo subprocesso.
     */
    private @org.jspecify.annotations.Nullable Long codUnidade;
}

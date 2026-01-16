package sgc.mapa.evento;

import lombok.Builder;
import lombok.Getter;
import sgc.mapa.model.Atividade;
import sgc.organizacao.model.Usuario;

import java.time.LocalDateTime;

/**
 * Evento de domínio publicado quando uma atividade é criada em um mapa de competências.
 *
 * <p>Este evento é disparado após a criação bem-sucedida de uma atividade,
 * permitindo que outros módulos detectem impactos e recalculem validações.
 *
 * <p><b>Casos de uso:</b>
 * <ul>
 *   <li>Detecção de impactos no mapa (novas competências necessárias)</li>
 *   <li>Validação automática do mapa atualizado</li>
 *   <li>Invalidação de cache de mapas</li>
 *   <li>Auditoria de mudanças em mapas</li>
 *   <li>Notificação de gestores sobre alterações</li>
 * </ul>
 *
 * <p><b>Relação com EventoMapaAlterado:</b>
 * <ul>
 *   <li><b>EventoAtividadeCriada:</b> Evento específico com dados da atividade</li>
 *   <li><b>EventoMapaAlterado:</b> Evento genérico indicando que o mapa mudou</li>
 *   <li>Ambos podem coexistir (EventoAtividadeCriada → listeners específicos, EventoMapaAlterado → cache)</li>
 * </ul>
 *
 * <p><b>Exemplo de uso:</b>
 * <pre>{@code
 * EventoAtividadeCriada evento = EventoAtividadeCriada.builder()
 *     .atividade(atividadeCriada)
 *     .codMapa(mapa.getCodigo())
 *     .codSubprocesso(subprocesso.getCodigo())
 *     .usuario(usuarioAutenticado)
 *     .dataHoraCriacao(LocalDateTime.now())
 *     .build();
 * eventPublisher.publishEvent(evento);
 * }</pre>
 *
 * @see EventoAtividadeAtualizada
 * @see EventoAtividadeExcluida
 * @see EventoMapaAlterado
 */
@Getter
@Builder
public class EventoAtividadeCriada {

    /**
     * A atividade que foi criada.
     */
    private Atividade atividade;

    /**
     * Código do mapa de competências ao qual a atividade foi adicionada.
     */
    private Long codMapa;

    /**
     * Código do subprocesso proprietário do mapa.
     */
    private Long codSubprocesso;

    /**
     * Usuário que criou a atividade.
     */
    private Usuario usuario;

    /**
     * Data e hora da criação.
     */
    private LocalDateTime dataHoraCriacao;

    /**
     * Número total de atividades no mapa após a criação.
     * <p>Útil para métricas e validações.
     */
    private int totalAtividadesNoMapa;
}

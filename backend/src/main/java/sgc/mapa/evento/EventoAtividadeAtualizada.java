package sgc.mapa.evento;

import lombok.Builder;
import lombok.Data;
import sgc.mapa.model.Atividade;
import sgc.organizacao.model.Usuario;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Evento de domínio publicado quando uma atividade é atualizada em um mapa de competências.
 *
 * <p>Este evento é disparado após a atualização bem-sucedida de uma atividade,
 * permitindo que outros módulos detectem impactos das mudanças.
 *
 * <p><b>Casos de uso:</b>
 * <ul>
 *   <li>Recálculo de impactos no mapa (mudanças em competências)</li>
 *   <li>Revalidação do mapa atualizado</li>
 *   <li>Invalidação de cache de mapas</li>
 *   <li>Auditoria de alterações</li>
 *   <li>Notificação de mudanças relevantes</li>
 * </ul>
 *
 * <p><b>Exemplo de uso:</b>
 * <pre>{@code
 * EventoAtividadeAtualizada evento = EventoAtividadeAtualizada.builder()
 *     .atividade(atividadeAtualizada)
 *     .codMapa(mapa.getCodigo())
 *     .codSubprocesso(subprocesso.getCodigo())
 *     .usuario(usuarioAutenticado)
 *     .camposAlterados(Set.of("descricao", "competencias"))
 *     .dataHoraAtualizacao(LocalDateTime.now())
 *     .build();
 * eventPublisher.publishEvent(evento);
 * }</pre>
 *
 * @see EventoAtividadeCriada
 * @see EventoAtividadeExcluida
 * @see EventoMapaAlterado
 */
@Data
@Builder
public class EventoAtividadeAtualizada {

    /**
     * A atividade que foi atualizada (estado atual).
     */
    private Atividade atividade;

    /**
     * Código do mapa de competências.
     */
    private Long codMapa;

    /**
     * Código do subprocesso proprietário do mapa.
     */
    private Long codSubprocesso;

    /**
     * Usuário que atualizou a atividade.
     */
    private Usuario usuario;

    /**
     * Campos que foram alterados na atualização.
     * <p>Exemplos: "descricao", "competencias", "conhecimentos"
     */
    private Set<String> camposAlterados;

    /**
     * Data e hora da atualização.
     */
    private LocalDateTime dataHoraAtualizacao;

    /**
     * Indica se a atualização afetou competências associadas.
     * <p>Se true, pode ser necessário recalcular impactos.
     */
    private boolean afetouCompetencias;
}

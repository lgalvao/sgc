package sgc.processo.eventos;

import lombok.Builder;
import lombok.Data;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Evento de domínio publicado quando um processo é atualizado.
 *
 * <p>Este evento é disparado após a atualização bem-sucedida de um processo,
 * permitindo que outros módulos reajam a mudanças na descrição, tipo, data limite
 * ou participantes do processo.
 *
 * <p><b>Casos de uso:</b>
 * <ul>
 *   <li>Auditoria de alterações em processos</li>
 *   <li>Notificação de gestores sobre mudanças</li>
 *   <li>Sincronização de cache/índices</li>
 *   <li>Atualização de painéis e relatórios</li>
 * </ul>
 *
 * <p><b>Exemplo de uso:</b>
 * <pre>{@code
 * EventoProcessoAtualizado evento = EventoProcessoAtualizado.builder()
 *     .processo(processoAtualizado)
 *     .usuario(usuarioAutenticado)
 *     .camposAlterados(Set.of("descricao", "dataLimite"))
 *     .dataHoraAtualizacao(LocalDateTime.now())
 *     .build();
 * eventPublisher.publishEvent(evento);
 * }</pre>
 *
 * @see EventoProcessoCriado
 * @see EventoProcessoExcluido
 */
@Data
@Builder
public class EventoProcessoAtualizado {

    /**
     * O processo que foi atualizado (estado atual após atualização).
     */
    private Processo processo;

    /**
     * Usuário que realizou a atualização.
     */
    private Usuario usuario;

    /**
     * Campos que foram alterados na atualização.
     * <p>Exemplos: "descricao", "tipo", "dataLimite", "participantes"
     */
    private Set<String> camposAlterados;

    /**
     * Data e hora da atualização.
     */
    private LocalDateTime dataHoraAtualizacao;

    /**
     * Tipo do processo antes da atualização (se o tipo foi alterado).
     * <p>Pode ser null se o tipo não foi alterado.
     */
    private TipoProcesso tipoAnterior;

    /**
     * Observações opcionais sobre a atualização.
     * <p>Pode conter justificativa ou contexto da mudança.
     */
    private String observacoes;
}

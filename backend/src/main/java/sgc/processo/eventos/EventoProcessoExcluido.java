package sgc.processo.eventos;

import lombok.Builder;
import lombok.Data;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.TipoProcesso;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Evento de domínio publicado quando um processo é excluído.
 *
 * <p>Este evento é disparado antes da exclusão física do processo no banco de dados,
 * permitindo que outros módulos realizem limpezas coordenadas ou registrem auditoria.
 *
 * <p><b>Casos de uso:</b>
 * <ul>
 *   <li>Auditoria de exclusões (trilha de remoções)</li>
 *   <li>Limpeza de dados relacionados (alertas, notificações)</li>
 *   <li>Invalidação de cache</li>
 *   <li>Notificação de gestores sobre exclusão</li>
 *   <li>Backup de dados antes da exclusão</li>
 * </ul>
 *
 * <p><b>Importante:</b> Este evento é publicado ANTES da exclusão física, então
 * listeners ainda podem acessar dados relacionados via queries se necessário.
 *
 * <p><b>Exemplo de uso:</b>
 * <pre>{@code
 * EventoProcessoExcluido evento = EventoProcessoExcluido.builder()
 *     .codProcesso(codigo)
 *     .descricao(processo.getDescricao())
 *     .tipo(processo.getTipo())
 *     .usuario(usuarioAutenticado)
 *     .codigosUnidades(processo.getParticipantes().stream()
 *         .map(Unidade::getCodigo)
 *         .collect(Collectors.toSet()))
 *     .dataHoraExclusao(LocalDateTime.now())
 *     .build();
 * eventPublisher.publishEvent(evento);
 * processoRepo.deleteById(codigo);
 * }</pre>
 *
 * @see EventoProcessoCriado
 * @see EventoProcessoAtualizado
 */
@Data
@Builder
public class EventoProcessoExcluido {

    /**
     * Código do processo que foi excluído.
     * <p>Armazenado separadamente pois a entidade será removida do banco.
     */
    private Long codProcesso;

    /**
     * Descrição do processo excluído.
     * <p>Para auditoria e logs.
     */
    private String descricao;

    /**
     * Tipo do processo excluído.
     */
    private TipoProcesso tipo;

    /**
     * Usuário que realizou a exclusão.
     */
    private Usuario usuario;

    /**
     * Códigos das unidades que participavam do processo.
     * <p>Para notificações e limpeza de dados relacionados.
     */
    private Set<Long> codigosUnidades;

    /**
     * Data e hora da exclusão.
     */
    private LocalDateTime dataHoraExclusao;

    /**
     * Motivo da exclusão (opcional).
     * <p>Pode conter justificativa fornecida pelo usuário.
     */
    private String motivoExclusao;
}

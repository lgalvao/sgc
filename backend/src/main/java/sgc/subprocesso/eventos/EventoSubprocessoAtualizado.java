package sgc.subprocesso.eventos;

import lombok.Builder;
import lombok.Getter;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Evento de domínio publicado quando um subprocesso é atualizado.
 *
 * <p>Este evento é disparado após a atualização bem-sucedida de campos básicos
 * de um subprocesso (não relacionados a transições de workflow, que usam
 * {@link EventoTransicaoSubprocesso}).
 *
 * <p><b>Casos de uso:</b>
 * <ul>
 *   <li>Sincronização de cache/índices</li>
 *   <li>Atualização de painéis</li>
 *   <li>Invalidação de permissões calculadas</li>
 *   <li>Auditoria de alterações</li>
 * </ul>
 *
 * <p><b>Diferença entre EventoSubprocessoAtualizado e EventoTransicaoSubprocesso:</b>
 * <ul>
 *   <li><b>EventoSubprocessoAtualizado:</b> Mudanças em dados (descrição, observações, etc.)</li>
 *   <li><b>EventoTransicaoSubprocesso:</b> Mudanças de estado/workflow (disponibilizar, devolver, homologar, etc.)</li>
 * </ul>
 *
 * <p><b>Exemplo de uso:</b>
 * <pre>{@code
 * EventoSubprocessoAtualizado evento = EventoSubprocessoAtualizado.builder()
 *     .subprocesso(subprocessoAtualizado)
 *     .usuario(usuarioAutenticado)
 *     .camposAlterados(Set.of("observacoes"))
 *     .dataHoraAtualizacao(LocalDateTime.now())
 *     .build();
 * eventPublisher.publishEvent(evento);
 * }</pre>
 *
 * @see EventoSubprocessoCriado
 * @see EventoSubprocessoExcluido
 * @see EventoTransicaoSubprocesso
 */
@Getter
@Builder
public class EventoSubprocessoAtualizado {

    /**
     * O subprocesso que foi atualizado (estado atual).
     */
    private Subprocesso subprocesso;

    /**
     * Usuário que realizou a atualização.
     */
    private Usuario usuario;

    /**
     * Campos que foram alterados na atualização.
     * <p>Exemplos: "observacoes", "dataLimite", "responsavel"
     */
    private Set<String> camposAlterados;

    /**
     * Data e hora da atualização.
     */
    private LocalDateTime dataHoraAtualizacao;

    /**
     * Situação anterior (se a situação foi alterada).
     * <p>Pode ser null se a situação não foi alterada diretamente.
     * <p><b>Nota:</b> Mudanças de situação via workflow devem usar {@link EventoTransicaoSubprocesso}.
     */
    private SituacaoSubprocesso situacaoAnterior;

    /**
     * Observações opcionais sobre a atualização.
     */
    private String observacoes;
}

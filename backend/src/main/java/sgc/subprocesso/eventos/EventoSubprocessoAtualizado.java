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
public class EventoSubprocessoAtualizado {
    private Subprocesso subprocesso;
    private @org.jspecify.annotations.Nullable Usuario usuario;
    private Set<String> camposAlterados;
    private LocalDateTime dataHoraAtualizacao;
    private SituacaoSubprocesso situacaoAnterior;
    private String observacoes;

    @Builder
    public EventoSubprocessoAtualizado(
            Subprocesso subprocesso,
            @org.jspecify.annotations.Nullable Usuario usuario,
            Set<String> camposAlterados,
            LocalDateTime dataHoraAtualizacao,
            SituacaoSubprocesso situacaoAnterior,
            String observacoes) {
        this.subprocesso = subprocesso;
        this.usuario = usuario;
        this.camposAlterados = camposAlterados;
        this.dataHoraAtualizacao = dataHoraAtualizacao;
        this.situacaoAnterior = situacaoAnterior;
        this.observacoes = observacoes;
    }
}

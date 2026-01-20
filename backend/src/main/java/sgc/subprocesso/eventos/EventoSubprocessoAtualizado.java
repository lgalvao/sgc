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
        this.camposAlterados = camposAlterados != null ? Set.copyOf(camposAlterados) : Set.of();
        this.dataHoraAtualizacao = dataHoraAtualizacao;
        this.situacaoAnterior = situacaoAnterior;
        this.observacoes = observacoes;
    }
}

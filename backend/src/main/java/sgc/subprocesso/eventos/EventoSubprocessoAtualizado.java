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
@Builder
public class EventoSubprocessoAtualizado {
    private Subprocesso subprocesso;
    private Usuario usuario;
    private Set<String> camposAlterados;
    private LocalDateTime dataHoraAtualizacao;
    private SituacaoSubprocesso situacaoAnterior;
    private String observacoes;
}

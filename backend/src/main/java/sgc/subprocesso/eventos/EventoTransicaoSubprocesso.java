package sgc.subprocesso.eventos;

import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.Subprocesso;

/**
 * Evento de domínio publicado quando ocorre uma transição de estado em um subprocesso.
 *
 * <p>Este evento único substitui as 19+ classes de evento anteriores (EventoSubprocessoCadastroDisponibilizado,
 * EventoSubprocessoCadastroDevolvido, etc.), centralizando a comunicação de transições.
 */
@Getter
@Builder
public class EventoTransicaoSubprocesso {
    private Subprocesso subprocesso;
    private TipoTransicao tipo;
    private @Nullable Usuario usuario;
    private Unidade unidadeOrigem;
    private Unidade unidadeDestino;
    @Nullable
    private String observacoes;
}


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
 */
@Getter
@Builder
public class EventoTransicaoSubprocesso {
    private Subprocesso subprocesso;
    private TipoTransicao tipo;
    private Usuario usuario;
    private Unidade unidadeOrigem;
    private Unidade unidadeDestino;

    @Nullable
    private String observacoes;
}


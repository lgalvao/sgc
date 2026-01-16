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

    /**
     * O subprocesso que transitou.
     */
    private Subprocesso subprocesso;

    /**
     * Tipo da transição, determinando comportamentos de comunicação.
     */
    private TipoTransicao tipo;

    /**
     * Usuário que executou a ação que causou a transição.
     */
    private Usuario usuario;

    /**
     * Unidade de origem da transição (de onde o subprocesso está saindo).
     */
    @Nullable
    private Unidade unidadeOrigem;

    /**
     * Unidade de destino da transição (para onde o subprocesso está indo).
     */
    @Nullable
    private Unidade unidadeDestino;

    /**
     * Observações opcionais associadas à transição.
     */
    @Nullable
    private String observacoes;
}


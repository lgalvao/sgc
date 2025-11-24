package sgc.processo.eventos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sgc.sgrh.model.Usuario;
import sgc.unidade.model.Unidade;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class EventoSubprocessoBase {
    private Long codSubprocesso;
    private Usuario usuario;
    private String observacoes;
    private Unidade unidadeOrigem;
    private Unidade unidadeDestino;
}

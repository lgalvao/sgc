package sgc.organizacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AtribuicaoTemporariaDto {

    private final Long codigo;
    private final UnidadeDto unidade;
    private final UsuarioDto usuario;
    private final LocalDateTime dataInicio;
    private final LocalDateTime dataTermino;
    private final String justificativa;
}

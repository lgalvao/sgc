package sgc.subprocesso.api;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ValidacaoCadastroDto {
    private Boolean valido;
    private List<ErroValidacaoDto> erros;
}

package sgc.subprocesso.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ValidacaoCadastroDto {
    private Boolean valido;
    private List<ErroValidacaoDto> erros;
}

package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ValidacaoCadastroDto {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private Boolean valido;
    private List<ErroValidacaoDto> erros;
}

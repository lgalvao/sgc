package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErroValidacaoDto {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private String tipo; // "ATIVIDADE_SEM_CONHECIMENTO", "SEM_ATIVIDADES", etc.
    private Long atividadeCodigo;
    private String descricaoAtividade;
    private String mensagem;
}

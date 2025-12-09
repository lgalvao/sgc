package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErroValidacaoDto {
    private String tipo; // "ATIVIDADE_SEM_CONHECIMENTO", "SEM_ATIVIDADES", etc.
    private Long atividadeId;
    private String descricaoAtividade;
    private String mensagem;
}

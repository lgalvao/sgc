package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sgc.seguranca.sanitizacao.SanitizarHtml;

/**
 * DTO para Atividade usado nas APIs (entrada/saída).
 */
@Getter
@Builder
@AllArgsConstructor
public class AtividadeDto {

    private final Long codigo;
    private final Long mapaCodigo;

    @NotBlank(message = "Descrição não pode ser vazia")
    @SanitizarHtml
    private final String descricao;
}

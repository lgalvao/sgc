package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.seguranca.sanitizacao.SanitizarHtml;

/**
 * DTO para Conhecimento usado nas APIs (entrada/saída). Contém apenas campos
 * primários e referência
 * por codigo para evitar expor entidades JPA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConhecimentoDto {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private Long codigo;

    @NotNull(message = "Código da atividade é obrigatório")
    private Long atividadeCodigo;

    @NotBlank(message = "Descrição não pode ser vazia")
    @SanitizarHtml
    private String descricao;
}

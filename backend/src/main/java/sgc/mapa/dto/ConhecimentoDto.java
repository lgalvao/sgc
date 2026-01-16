package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sgc.seguranca.sanitizacao.SanitizarHtml;

/**
 * DTO para Conhecimento usado nas APIs (entrada/saída). Contém apenas campos
 * primários e referência por codigo para evitar expor entidades JPA.
 */
@Getter
@Builder
@AllArgsConstructor
public class ConhecimentoDto {

    private final Long codigo;

    @NotNull(message = "Código da atividade é obrigatório")
    private final Long atividadeCodigo;

    @NotBlank(message = "Descrição não pode ser vazia")
    @SanitizarHtml
    private final String descricao;
}

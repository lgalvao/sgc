package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.seguranca.sanitizacao.SanitizarHtml;

/**
 * DTO para Conhecimento usado nas APIs (entrada/saída). Contém apenas campos
 * primários e referência por codigo para evitar expor entidades JPA.
 * 
 * <p>Requer @NoArgsConstructor e @Setter para deserialização Jackson em endpoints de entrada.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConhecimentoDto {

    private Long codigo;

    @NotNull(message = "Código da atividade é obrigatório")
    private Long atividadeCodigo;

    @NotBlank(message = "Descrição não pode ser vazia")
    @SanitizarHtml
    private String descricao;
}

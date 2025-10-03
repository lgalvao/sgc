package sgc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para Conhecimento usado nas APIs (entrada/saída).
 * Contém apenas campos primários e referência por id para evitar expor entidades JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConhecimentoDTO {
    private Long codigo;

    @NotNull(message = "Código da atividade é obrigatório")
    private Long atividadeCodigo;

    @NotBlank(message = "Descrição não pode ser vazia")
    private String descricao;
}
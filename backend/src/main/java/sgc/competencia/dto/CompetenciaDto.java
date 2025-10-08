package sgc.competencia.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para Competência usado nas APIs (entrada/saída).
 * Contém apenas campos primários e referências por id para evitar expor entidades JPA.
 * Nota: mapaCodigo é opcional para compatibilidade com clientes existentes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompetenciaDto {
    private Long codigo;

    private Long mapaCodigo;

    @NotBlank(message = "Descrição não pode ser vazia")
    private String descricao;
}

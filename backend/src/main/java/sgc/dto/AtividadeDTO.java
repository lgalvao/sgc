package sgc.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para Atividade usado nas APIs (entrada/saída).
 * Contém apenas campos primários e referências por id para evitar expor entidades JPA.
 * Nota: mapaCodigo é opcional para compatibilidade com clientes existentes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeDTO {
    private Long codigo;

    private Long mapaCodigo;

    @NotBlank(message = "descricao não pode ser vazia")
    private String descricao;
}
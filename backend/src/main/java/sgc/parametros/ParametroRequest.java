package sgc.parametros;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de requisição para atualização de parâmetro de configuração.
 * Utilizado na atualização em bloco de parâmetros.
 */
public record ParametroRequest(
    @NotNull(message = "O código do parâmetro é obrigatório")
    Long codigo,
    
    @NotBlank(message = "A chave não pode estar vazia")
    @Size(max = 50, message = "A chave deve ter no máximo 50 caracteres")
    String chave,
    
    String descricao,
    
    @NotBlank(message = "O valor não pode estar vazio")
    String valor
) {
}

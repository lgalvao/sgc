package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.seguranca.sanitizacao.SanitizarHtml;

/**
 * DTO para Atividade usado nas APIs (entrada/saída).
 * 
 * <p>Requer @NoArgsConstructor e @Setter para deserialização Jackson em endpoints de entrada.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeDto {

    private Long codigo;
    private Long mapaCodigo;

    @NotBlank(message = "Descrição não pode ser vazia")
    @SanitizarHtml
    private String descricao;
}

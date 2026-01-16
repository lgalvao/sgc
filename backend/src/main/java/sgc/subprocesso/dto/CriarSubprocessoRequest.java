package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO de requisição para criar um novo subprocesso.
 * 
 * <p>Usado exclusivamente como entrada de API para o endpoint de criação.
 * A validação Bean Validation é aplicada neste DTO.
 * 
 * <p>Requer @NoArgsConstructor e @Setter para deserialização Jackson.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriarSubprocessoRequest {

    @NotNull(message = "O código do processo é obrigatório")
    private Long codProcesso;

    @NotNull(message = "O código da unidade é obrigatório")
    private Long codUnidade;

    private Long codMapa;

    private LocalDateTime dataLimiteEtapa1;
    private LocalDateTime dataLimiteEtapa2;
}

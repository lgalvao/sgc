package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de requisição para criar um novo subprocesso.
 * 
 * <p>Usado exclusivamente como entrada de API para o endpoint de criação.
 * A validação Bean Validation é aplicada neste DTO.
 */
@Data
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

package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO de requisição para atualizar um subprocesso existente.
 * 
 * <p>Usado exclusivamente como entrada de API para o endpoint de atualização.
 * Diferente da criação, não requer codProcesso pois o subprocesso já existe.
 * 
 * <p>Requer @NoArgsConstructor e @Setter para deserialização Jackson.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarSubprocessoRequest {
    private Long codUnidade;
    private Long codMapa;

    private LocalDateTime dataLimiteEtapa1;
    private LocalDateTime dataFimEtapa1;

    private LocalDateTime dataLimiteEtapa2;
    private LocalDateTime dataFimEtapa2;
}

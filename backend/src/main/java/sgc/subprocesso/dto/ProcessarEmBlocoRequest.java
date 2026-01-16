package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de requisição para processar subprocessos em bloco.
 * 
 * <p>Requer @NoArgsConstructor e @Setter para deserialização Jackson.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessarEmBlocoRequest {
    private List<Long> unidadeCodigos;
    private LocalDate dataLimite;
}

package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de requisição para processar subprocessos em bloco.
 */
@Getter
@Builder
@AllArgsConstructor
public class ProcessarEmBlocoRequest {
    private final List<Long> unidadeCodigos;
    private final LocalDate dataLimite;
}

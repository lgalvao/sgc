package sgc.subprocesso.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProcessarEmBlocoRequest {
    private List<Long> unidadeCodigos;
    private LocalDate dataLimite;
}

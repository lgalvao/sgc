package sgc.subprocesso.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class ProcessarEmBlocoRequest {
    private List<Long> unidadeCodigos;
    private LocalDate dataLimite;
}

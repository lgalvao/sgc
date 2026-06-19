package sgc.processo.dto;

import java.time.LocalDate;
import java.util.List;

public record DisponibilizarMapaEmBlocoCommand(
        List<Long> unidadeCodigos,
        LocalDate dataLimite
) implements AcaoEmBlocoCommand {
}

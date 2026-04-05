package sgc.processo.dto;

import java.time.*;
import java.util.*;

public record DisponibilizarMapaEmBlocoCommand(
        List<Long> unidadeCodigos,
        LocalDate dataLimite
) implements AcaoEmBlocoCommand {
}

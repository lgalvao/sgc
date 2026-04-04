package sgc.processo.dto;

import org.jspecify.annotations.*;

import java.time.*;
import java.util.*;

public record DisponibilizarMapaEmBlocoCommand(
        List<Long> unidadeCodigos,
        @Nullable LocalDate dataLimite
) implements AcaoEmBlocoCommand {
}

package sgc.processo.dto;

import java.util.*;

public sealed interface AcaoEmBlocoCommand permits DisponibilizarMapaEmBlocoCommand, ProcessarAnaliseEmBlocoCommand {
    List<Long> unidadeCodigos();
}

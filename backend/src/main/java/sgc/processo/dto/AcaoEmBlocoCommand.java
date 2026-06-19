package sgc.processo.dto;

import java.util.List;

public sealed interface AcaoEmBlocoCommand permits DisponibilizarMapaEmBlocoCommand, ProcessarAnaliseEmBlocoCommand {
    List<Long> unidadeCodigos();
}

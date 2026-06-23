package sgc.processo.dto;

import sgc.processo.model.*;

import java.util.*;

public record ProcessarAnaliseEmBlocoCommand(
        List<Long> unidadeCodigos,
        AcaoProcesso acao
) implements AcaoEmBlocoCommand {
}

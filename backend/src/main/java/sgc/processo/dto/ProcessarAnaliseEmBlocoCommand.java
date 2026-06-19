package sgc.processo.dto;

import sgc.processo.model.AcaoProcesso;

import java.util.List;

public record ProcessarAnaliseEmBlocoCommand(
        List<Long> unidadeCodigos,
        AcaoProcesso acao
) implements AcaoEmBlocoCommand {
}

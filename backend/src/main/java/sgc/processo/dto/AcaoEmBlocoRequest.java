package sgc.processo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import sgc.processo.model.AcaoProcesso;

import java.time.LocalDate;
import java.util.List;

public record AcaoEmBlocoRequest(
    @NotEmpty(message = "Pelo menos uma unidade deve ser selecionada")
    List<Long> unidadeCodigos,

    @NotNull(message = "A ação deve ser informada")
    AcaoProcesso acao,

    LocalDate dataLimite
) {}

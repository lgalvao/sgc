package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CompetenciaReq(
    @NotBlank String descricao,
    List<Long> atividadesIds
) {}

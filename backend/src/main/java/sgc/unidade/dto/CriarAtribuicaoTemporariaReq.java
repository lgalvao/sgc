package sgc.unidade.dto;

import java.time.LocalDate;

public record CriarAtribuicaoTemporariaReq(
        String tituloEleitoralUsuario,
        LocalDate dataTermino,
        String justificativa) {
}

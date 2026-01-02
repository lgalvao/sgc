package sgc.organizacao.dto;

import java.time.LocalDate;

public record CriarAtribuicaoTemporariaReq(
        String tituloEleitoralUsuario,
        LocalDate dataInicio,
        LocalDate dataTermino,
        String justificativa) {
}

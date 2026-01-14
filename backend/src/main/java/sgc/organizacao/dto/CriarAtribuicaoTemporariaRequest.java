package sgc.organizacao.dto;

import java.time.LocalDate;

public record CriarAtribuicaoTemporariaRequest(
        String tituloEleitoralUsuario,
        LocalDate dataInicio,
        LocalDate dataTermino,
        String justificativa) {
}

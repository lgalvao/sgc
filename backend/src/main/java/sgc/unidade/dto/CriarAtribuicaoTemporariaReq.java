package sgc.unidade.dto;

import java.time.LocalDate;

public record CriarAtribuicaoTemporariaReq(
        String tituloEleitoralServidor, LocalDate dataTermino, String justificativa) {
}

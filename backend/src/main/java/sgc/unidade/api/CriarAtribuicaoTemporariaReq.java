package sgc.unidade.api;

import java.time.LocalDate;

public record CriarAtribuicaoTemporariaReq(
        String tituloEleitoralUsuario, LocalDate dataTermino, String justificativa) {
}

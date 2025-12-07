package sgc.unidade.dto;

import java.time.LocalDate;

public record CriarAtribuicaoTemporariaRequest(
        String tituloEleitoralServidor, LocalDate dataTermino, String justificativa) {}

package sgc.organizacao.dto;

import sgc.seguranca.sanitizacao.SanitizarHtml;

import java.time.LocalDate;

public record CriarAtribuicaoTemporariaRequest(
        String tituloEleitoralUsuario,
        LocalDate dataInicio,
        LocalDate dataTermino,
        @SanitizarHtml
        String justificativa) {
}

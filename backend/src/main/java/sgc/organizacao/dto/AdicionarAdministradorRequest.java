package sgc.organizacao.dto;

import sgc.comum.model.TituloEleitoral;

public record AdicionarAdministradorRequest(
        @TituloEleitoral
        String tituloEleitoral) {
}

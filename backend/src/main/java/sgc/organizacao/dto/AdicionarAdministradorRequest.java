package sgc.organizacao.dto;

import sgc.comum.model.*;

public record AdicionarAdministradorRequest(
        @TituloEleitoral
        String tituloEleitoral) {
}

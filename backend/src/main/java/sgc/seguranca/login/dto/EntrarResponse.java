package sgc.seguranca.login.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sgc.organizacao.model.Perfil;

/**
 * DTO para resposta de login.
 */
@Getter
@Builder
@AllArgsConstructor
public class EntrarResponse {
    private final String tituloEleitoral;
    private final String nome;
    private final Perfil perfil;
    private final Long unidadeCodigo;
    private final String token;
}

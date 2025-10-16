package sgc.sgrh.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import sgc.sgrh.Perfil;

@Value
@Builder
@Jacksonized
public class UsuarioDto {
    String nome;
    Long tituloEleitoral;
    Perfil perfil;
    String unidade;
    String token;
}
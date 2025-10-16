package sgc.sgrh.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class LoginResponse {
    String nome;
    Long tituloEleitoral;
    List<PerfilUnidade> pares;
}
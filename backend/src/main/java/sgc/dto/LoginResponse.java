package sgc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private List<PerfilUnidadeDTO> perfis;
    private List<PerfilUnidadeDTO> unidades;
}
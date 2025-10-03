package sgc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Título não pode ser vazio")
    private String titulo;

    @NotBlank(message = "Senha não pode ser vazia")
    private String senha;
}
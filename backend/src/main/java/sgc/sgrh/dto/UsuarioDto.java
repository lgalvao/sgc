package sgc.sgrh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para dados de usuário do SGRH.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDto {
    private String titulo;
    private String nome;
    private String email;
    private String matricula;
}

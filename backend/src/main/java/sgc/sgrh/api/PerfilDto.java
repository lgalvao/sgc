package sgc.sgrh.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para perfil de usuário em uma unidade.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilDto {
    private String usuarioTitulo;
    private Long unidadeCodigo;
    private String unidadeNome;
    private String perfil; // ADMIN, GESTOR, CHEFE, SERVIDOR
}

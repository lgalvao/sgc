package sgc.organizacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO para perfil de usuário em uma unidade.
 */
@Getter
@Builder
@AllArgsConstructor
public class PerfilDto {
    private final String usuarioTitulo;
    private final Long unidadeCodigo;
    private final String unidadeNome;
    private final String perfil; // ADMIN, GESTOR, CHEFE, SERVIDOR
}

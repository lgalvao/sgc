package sgc.organizacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO para dados de usuário do SGRH.
 *
 * <p>Usado nos endpoints que retornam dados de usuários,
 * incluindo GET /api/unidades/{codigoUnidade}/usuarios.
 */
@Getter
@Builder
@AllArgsConstructor
public class UsuarioDto {

    private final String nome;
    private final String tituloEleitoral;
    private final String email;
    private final String matricula;

    /** Código da unidade de lotação do usuário. */
    private final Long unidadeCodigo;
}

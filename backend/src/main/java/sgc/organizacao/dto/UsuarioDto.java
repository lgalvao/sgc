package sgc.organizacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para dados de usuário do SGRH.
 *
 * <p>Usado nos endpoints que retornam dados de usuários,
 * incluindo GET /api/unidades/{codigoUnidade}/usuarios.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDto {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private String nome;
    private String tituloEleitoral;
    private String email;
    private String matricula;

    /** Código da unidade de lotação do usuário. */
    private Long unidadeCodigo;
}

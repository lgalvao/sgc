package sgc.sgrh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para dados de usuário do SGRH.
 *
 * <p>Usado nos endpoints que retornam dados de usuários/servidores,
 * incluindo GET /api/unidades/{codigoUnidade}/usuarios.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDto {
    /** Código do usuário (mesmo que título eleitoral, para retrocompatibilidade). */
    private String codigo;

    private String nome;

    /** Título eleitoral (identificador principal do usuário). */
    private String tituloEleitoral;

    private String email;

    private String matricula;

    /** Código da unidade de lotação do usuário. */
    private Long unidadeCodigo;
}

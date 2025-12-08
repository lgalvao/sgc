package sgc.sgrh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para dados de um usuário (servidor) retornados para o frontend.
 *
 * <p>Usado no endpoint GET /api/unidades/{codigoUnidade}/servidores para retornar a lista de
 * usuários pertencentes a uma unidade específica.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServidorDto {
    private String codigo;
    private String nome;
    private String tituloEleitoral;
    private String email;
    private Long unidadeCodigo;
}

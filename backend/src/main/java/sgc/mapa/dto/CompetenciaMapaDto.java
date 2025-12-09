package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa uma competência no contexto do mapa completo. Contém os dados da competência e
 * os códigos das atividades vinculadas.
 */
@Data
@Builder
@NoArgsConstructor
public class CompetenciaMapaDto {
    /** O código da competência (pode ser nulo para novas competências). */
    private Long codigo;

    /** A descrição da competência. */
    @NotBlank(message = "Descrição da competência é obrigatória")
    private String descricao;

    /** Lista com os códigos das atividades vinculadas à competência. */
    @NotNull(message = "Lista de atividades não pode ser nula")
    private List<Long> atividadesCodigos;

    public CompetenciaMapaDto(Long codigo, String descricao, List<Long> atividadesCodigos) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.atividadesCodigos =
                (atividadesCodigos == null) ? null : new ArrayList<>(atividadesCodigos);
    }

    public void setAtividadesCodigos(List<Long> atividadesCodigos) {
        this.atividadesCodigos =
                (atividadesCodigos == null) ? null : new ArrayList<>(atividadesCodigos);
    }

    public List<Long> getAtividadesCodigos() {
        return (atividadesCodigos == null) ? null : new ArrayList<>(atividadesCodigos);
    }
}

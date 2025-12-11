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
    /**
     * O código da competência (pode ser nulo para novas competências).
     */
    private Long codigo;

    /**
     * A descrição da competência.
     */
    @NotBlank(message = "Descrição da competência é obrigatória")
    private String descricao;

    /**
     * Lista com os códigos das atividades vinculadas à competência.
     */
    @NotNull(message = "Lista de atividades não pode ser nula")
    private List<Long> atividadesAssociadas;

    public CompetenciaMapaDto(Long codigo, String descricao, List<Long> atividadesAssociadas) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.atividadesAssociadas =
                (atividadesAssociadas == null) ? null : new ArrayList<>(atividadesAssociadas);
    }

    public List<Long> getAtividadesAssociadas() {
        return (atividadesAssociadas == null) ? null : new ArrayList<>(atividadesAssociadas);
    }

    public void setAtividadesAssociadas(List<Long> atividadesAssociadas) {
        this.atividadesAssociadas =
                (atividadesAssociadas == null) ? null : new ArrayList<>(atividadesAssociadas);
    }
}

package sgc.processo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO usado para atualizar um processo existente.
 *
 * @param codigo O código do processo.
 * @param descricao A descrição do processo.
 * @param tipo O tipo do processo.
 * @param dataLimiteEtapa1 A data limite para a primeira etapa.
 * @param unidades A lista de códigos das unidades participantes.
 */
public record AtualizarProcessoReq(
    Long codigo,

    @NotBlank(message = "Preencha a descrição")
    String descricao,

    @jakarta.validation.constraints.NotNull(message = "Tipo do processo é obrigatório")
    sgc.processo.modelo.TipoProcesso tipo,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime dataLimiteEtapa1,

    @NotEmpty(message = "Pelo menos uma unidade participante deve ser incluída.")
    List<Long> unidades
) {
    /**
     * Construtor compacto para garantir a imutabilidade da lista de unidades.
     */
    public AtualizarProcessoReq {
        unidades = new ArrayList<>(unidades);
    }

    /**
     * Retorna uma cópia da lista de unidades para garantir a imutabilidade.
     * @return A lista de unidades.
     */
    @Override
    public List<Long> unidades() {
        return new ArrayList<>(unidades);
    }
}

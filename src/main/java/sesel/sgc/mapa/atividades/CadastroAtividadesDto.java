package sesel.sgc.mapa.atividades;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO para a requisição de cadastro de atividades de uma unidade em um
 * processo.
 */
@Data
public class CadastroAtividadesDto {
    @NotNull(message = "ID da Processo de unidade é obrigatório")
    private UUID processoUnidadeId;

    @Valid
    private List<AtividadeDto> atividades = new ArrayList<>();

    private boolean rascunho = false;

    private String novaAtividadeDescricao;
}

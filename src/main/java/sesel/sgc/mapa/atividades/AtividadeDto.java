package sesel.sgc.mapa.atividades;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sesel.sgc.mapa.conhecimentos.ConhecimentoTipo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO para representar uma Atividade no sistema.
 */
@Data
public class AtividadeDto {
    private UUID id;

    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    private UUID competenciaId;
    private UUID processoUnidadeId;
    private List<ConhecimentoDto> conhecimentos = new ArrayList<>();

    // Campo auxiliar para o formulário (não persistente)
    private String novoConhecimento;

    // Campo para controle de edição no formulário
    private boolean editando;

    // Campo para controle de adição de conhecimento no formulário
    private boolean adicionandoConhecimento;

    /**
     * DTO para representar um Conhecimento associado a uma Atividade.
     */
    @Data
    public static class ConhecimentoDto {
        private UUID id;

        @NotBlank(message = "Descrição é obrigatória")
        private String descricao;

        @NotNull(message = "Tipo de conhecimento é obrigatório")
        private ConhecimentoTipo tipo;

        private UUID atividadeId;

        // Métodos de conversão manual
        public static ConhecimentoDto fromEntity(sesel.sgc.mapa.conhecimentos.Conhecimento conhecimento) {
            if (conhecimento == null)
                return null;
            ConhecimentoDto dto = new ConhecimentoDto();
            dto.setId(conhecimento.getId());
            dto.setDescricao(conhecimento.getDescricao());
            dto.setTipo(conhecimento.getTipo());
            dto.setAtividadeId(conhecimento.getAtividade() != null ? conhecimento.getAtividade().getId() : null);
            return dto;
        }

        public sesel.sgc.mapa.conhecimentos.Conhecimento toEntity() {
            sesel.sgc.mapa.conhecimentos.Conhecimento conhecimento = new sesel.sgc.mapa.conhecimentos.Conhecimento();
            conhecimento.setDescricao(this.getDescricao());
            conhecimento.setTipo(this.getTipo());
            return conhecimento;
        }

        public void updateEntity(sesel.sgc.mapa.conhecimentos.Conhecimento conhecimento) {
            if (this.getDescricao() != null) conhecimento.setDescricao(this.getDescricao());

            if (this.getTipo() != null) conhecimento.setTipo(this.getTipo());
        }
    }
}

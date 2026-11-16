package sesel.sgc.mapa.conhecimentos;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import sesel.sgc.EntidadeBase;
import sesel.sgc.mapa.atividades.Atividade;

@Entity
@Table(name = "conhecimento")
@Getter
@Setter
public class Conhecimento extends EntidadeBase {
    @NotNull(message = "Atividade é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "atividade_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_conhecimento_atividade")
    )
    private Atividade atividade;

    @NotBlank(message = "Descrição é obrigatória")
    @Column(nullable = false)
    private String descricao;

    @NotNull(message = "Tipo de conhecimento é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConhecimentoTipo tipo;
}

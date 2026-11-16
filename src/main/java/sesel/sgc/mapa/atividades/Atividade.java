package sesel.sgc.mapa.atividades;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import sesel.sgc.EntidadeBase;
import sesel.sgc.mapa.Competencia;
import sesel.sgc.mapa.conhecimentos.Conhecimento;
import sesel.sgc.processounidade.ProcessoUnidade;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "atividade")
@Getter
@Setter
public class Atividade extends EntidadeBase {
    @NotBlank(message = "Descrição é obrigatória")
    @Column(nullable = false)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "competencia_id",
            foreignKey = @ForeignKey(name = "fk_atividade_competencia")
    )
    private Competencia competencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "processo_unidade_id",
            foreignKey = @ForeignKey(name = "fk_atividade_processoUnidade")
    )
    private ProcessoUnidade processoUnidade;

    @OneToMany(
            mappedBy = "atividade",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<Conhecimento> conhecimentos = new ArrayList<>();

    @Override
    public String toString() {
        return "Atividade{descricao='%s'}".formatted(descricao);
    }
}

package sgc.mapa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sgc.comum.model.EntidadeBase;

@Entity
@Table(name = "CONHECIMENTO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Conhecimento extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "atividade_codigo", nullable = false)
    private Atividade atividade;

    @Column(name = "descricao", nullable = false)
    private String descricao;

    public Long getCodigoAtividade() {
        return atividade.getCodigo();
    }
}

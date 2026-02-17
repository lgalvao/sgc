package sgc.mapa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
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
    @JsonIgnore
    private Atividade atividade;

    @JsonView(MapaViews.Minimal.class)
    @Column(name = "descricao", nullable = false)
    private String descricao;

    @JsonView(MapaViews.Publica.class)
    @JsonProperty("atividadeCodigo")
    public Long getCodigoAtividade() {
        return atividade.getCodigo();
    }
}

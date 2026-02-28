package sgc.mapa.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import sgc.comum.model.*;

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
    @JsonIgnoreProperties("conhecimentos")
    private Atividade atividade;

    @JsonView({MapaViews.Minimal.class, MapaViews.Publica.class})
    @Column(name = "descricao", nullable = false)
    private String descricao;

    public static Conhecimento criarDe(sgc.mapa.dto.CriarConhecimentoRequest request) {
        return Conhecimento.builder()
                .descricao(request.descricao())
                .build();
    }

    @JsonView(MapaViews.Publica.class)
    @JsonProperty("atividadeCodigo")
    public Long getCodigoAtividade() {
        return atividade.getCodigo();
    }

    public void atualizarDe(sgc.mapa.dto.AtualizarConhecimentoRequest request) {
        this.descricao = request.descricao();
    }
}

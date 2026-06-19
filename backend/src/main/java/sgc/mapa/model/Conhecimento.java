package sgc.mapa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@SuppressWarnings("NullAway.Init")
public class Conhecimento extends EntidadeBase {
    @ManyToOne(fetch = FetchType.LAZY)
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

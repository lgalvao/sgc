package sgc.alerta.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import sgc.comum.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;

import java.time.*;

@Entity
@Table(name = "ALERTA", schema = "sgc")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Alerta extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "processo_codigo", nullable = false)
    @JsonIgnore
    private Processo processo;

    @JsonView(ComumViews.Publica.class)
    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @ManyToOne
    @JoinColumn(name = "unidade_origem_codigo", nullable = false)
    @JsonIgnore
    private Unidade unidadeOrigem;

    @ManyToOne
    @JoinColumn(name = "unidade_destino_codigo", nullable = false)
    @JsonIgnore
    private Unidade unidadeDestino;

    @JsonView(ComumViews.Publica.class)
    @Column(name = "descricao", nullable = false)
    private String descricao;

    @Transient
    @JsonView(ComumViews.Publica.class)
    private LocalDateTime dataHoraLeitura;

    @JsonView(ComumViews.Publica.class)
    @JsonProperty("codProcesso")
    public Long getCodProcessoSintetico() {
        return processo.getCodigo();
    }

    @JsonView(ComumViews.Publica.class)
    @JsonProperty("processo")
    public String getProcessoDescricaoSintetica() {
        return processo.getDescricao();
    }

    @JsonView(ComumViews.Publica.class)
    @JsonProperty("origem")
    public String getOrigemSiglaSintetica() {
        return unidadeOrigem.getSigla();
    }

    @JsonView(ComumViews.Publica.class)
    @JsonProperty("unidadeDestino")
    public String getUnidadeDestinoSigla() {
        return unidadeDestino.getSigla();
    }

    @JsonView(ComumViews.Publica.class)
    @JsonProperty("mensagem")
    public String getMensagemSintetica() {
        return descricao;
    }
}

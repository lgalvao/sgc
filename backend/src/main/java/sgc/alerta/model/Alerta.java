package sgc.alerta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sgc.comum.model.ComumViews;
import sgc.comum.model.EntidadeBase;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;

import java.time.LocalDateTime;

/**
 * Representa um alerta ou notificação dentro do sistema.
 */
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
    @JoinColumn(name = "unidade_destino_codigo")
    @JsonIgnore
    private Unidade unidadeDestino;

    @ManyToOne
    @JoinColumn(name = "usuario_destino_titulo")
    @JsonIgnore
    private Usuario usuarioDestino;

    @JsonView(ComumViews.Publica.class)
    @Column(name = "descricao", nullable = false)
    private String descricao;

    // ========== Campos Transientes para API (Flattening) ==========

    @Transient
    @JsonView(ComumViews.Publica.class)
    private LocalDateTime dataHoraLeitura;

    @JsonView(ComumViews.Publica.class)
    @JsonProperty("codProcesso")
    public Long getCodProcessoSintetico() {
        return processo != null ? processo.getCodigo() : null;
    }

    @JsonView(ComumViews.Publica.class)
    @JsonProperty("processo")
    public String getProcessoDescricaoSintetica() {
        return processo != null ? processo.getDescricao() : null;
    }

    @JsonView(ComumViews.Publica.class)
    @JsonProperty("origem")
    public String getOrigemSiglaSintetica() {
        if (unidadeOrigem == null) return null;
        return Long.valueOf(1L).equals(unidadeOrigem.getCodigo()) ? "ADMIN" : unidadeOrigem.getSigla();
    }

    @JsonView(ComumViews.Publica.class)
    @JsonProperty("unidadeOrigem")
    public String getUnidadeOrigemSigla() {
        return getOrigemSiglaSintetica();
    }

    @JsonView(ComumViews.Publica.class)
    @JsonProperty("unidadeDestino")
    public String getUnidadeDestinoSigla() {
        return unidadeDestino != null ? unidadeDestino.getSigla() : null;
    }

    @JsonView(ComumViews.Publica.class)
    @JsonProperty("mensagem")
    public String getMensagemSintetica() {
        return descricao;
    }
}

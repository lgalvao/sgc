package sgc.subprocesso.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sgc.comum.model.ComumViews;
import sgc.comum.model.EntidadeBase;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;

import java.time.LocalDateTime;

@Entity
@Table(name = "MOVIMENTACAO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Movimentacao extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "subprocesso_codigo", nullable = false)
    @JsonView(ComumViews.Publica.class)
    private Subprocesso subprocesso;

    @Builder.Default
    @Column(name = "data_hora", nullable = false)
    @JsonView(ComumViews.Publica.class)
    private LocalDateTime dataHora = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "unidade_origem_codigo", nullable = false)
    @JsonView(ComumViews.Publica.class)
    private Unidade unidadeOrigem;

    @ManyToOne
    @JoinColumn(name = "unidade_destino_codigo", nullable = false)
    @JsonView(ComumViews.Publica.class)
    private Unidade unidadeDestino;

    @Column(name = "descricao")
    @JsonView(ComumViews.Publica.class)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_titulo", nullable = false)
    @JsonView(ComumViews.Publica.class)
    private Usuario usuario;
}

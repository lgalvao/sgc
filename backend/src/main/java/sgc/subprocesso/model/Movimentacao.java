package sgc.subprocesso.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import sgc.comum.model.*;
import sgc.organizacao.model.*;

import java.time.*;

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

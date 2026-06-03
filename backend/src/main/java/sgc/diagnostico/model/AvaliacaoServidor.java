package sgc.diagnostico.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;

@Entity
@Table(name = "AVALIACAO_SERVIDOR", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AttributeOverride(name = "codigo", column = @Column(name = "codigo"))
public class AvaliacaoServidor extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostico_codigo", nullable = false)
    private Diagnostico diagnostico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servidor_titulo", referencedColumnName = "titulo", nullable = false)
    private Usuario servidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competencia_codigo", nullable = false)
    private Competencia competencia;

    @Column(name ="importancia")
    private Integer importancia;

    @Column(name = "dominio")
    private Integer dominio;

    @Column(name = "gap")
    private Integer gap;

    @Column(name = "observacoes")
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_servidor", length = 50, nullable = false)
    private SituacaoAvaliacaoServidor situacaoServidor;

}

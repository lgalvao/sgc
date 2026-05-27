package sgc.diagnostico.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sgc.comum.model.EntidadeBase;
import sgc.mapa.model.Competencia;
import sgc.organizacao.model.Usuario;

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

    @Enumerated(EnumType.STRING)
    @Column(name ="importancia", length = 10)
    private NivelAvaliacao importancia;

    @Enumerated(EnumType.STRING)
    @Column(name = "dominio", length = 10)
    private NivelAvaliacao dominio;

    @Column(name = "gap")
    private Integer gap;

    @Column(name = "observacao")
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_servidor", length = 50, nullable = false)
    private SituacaoAvaliacaoServidor situacaoServidor;

    /**
     * Calcula e atualiza o valor do GAP (Importância - Domínio)
     * desconsiderando casos onde Importância ou Domínio sejam NA.
     */

    public void calculaGap() {
        if(importancia == null || dominio == null ||
                importancia == NivelAvaliacao.NA || dominio == NivelAvaliacao.NA ){
            this.gap = null;
        }else{
            int valorImportancia = Integer.parseInt(importancia.name().substring(1));
            int valorDominio = Integer.parseInt(dominio.name().substring(1));
            this.gap = valorImportancia - valorDominio;
        }
    }

}

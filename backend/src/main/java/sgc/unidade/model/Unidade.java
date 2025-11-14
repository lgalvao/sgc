package sgc.unidade.model;

import jakarta.persistence.*;
import lombok.*;
import sgc.comum.model.EntidadeBase;
import sgc.processo.model.Processo;
import sgc.sgrh.model.Usuario;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "UNIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unidade extends EntidadeBase {
    public Unidade(String nome, String sigla) {
        super();
        this.nome = nome;
        this.sigla = sigla;
        this.situacao = SituacaoUnidade.ATIVA;
        this.tipo = TipoUnidade.OPERACIONAL;
    }

    public Unidade(String nome, String sigla, Usuario titular, TipoUnidade tipo, SituacaoUnidade situacao, Unidade unidadeSuperior) {
        super();
        this.nome = nome;
        this.sigla = sigla;
        this.titular = titular;
        this.tipo = tipo;
        this.situacao = situacao;
        this.unidadeSuperior = unidadeSuperior;
    }

    @Column(name = "nome")
    private String nome;

    @Column(name = "sigla", length = 20)
    private String sigla;

    @ManyToOne
    @JoinColumn(name = "titular_titulo")
    private Usuario titular;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20)
    private TipoUnidade tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", length = 20)
    private SituacaoUnidade situacao;

    @ManyToOne
    @JoinColumn(name = "unidade_superior_codigo")
    private Unidade unidadeSuperior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mapa_vigente_codigo")
    private sgc.mapa.model.Mapa mapaVigente;

    @Column(name = "data_vigencia_mapa_atual")
    private java.time.LocalDateTime dataVigenciaMapaAtual;

    @ManyToMany(mappedBy = "participantes")
    @Builder.Default
    private Set<Processo> processos = new HashSet<>();
}
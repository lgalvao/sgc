package sgc.unidade.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.model.EntidadeBase;
import sgc.sgrh.model.Usuario;

@Entity
@Table(name = "UNIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
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
}
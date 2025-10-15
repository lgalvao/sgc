package sgc.unidade.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.modelo.EntidadeBase;
import sgc.sgrh.Usuario;

@Entity
@Table(name = "UNIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    public Unidade(Unidade unidade) {
        if (unidade != null) {
            super.setCodigo(unidade.getCodigo());
            this.nome = unidade.getNome();
            this.sigla = unidade.getSigla();
            this.titular = unidade.getTitular();
            this.tipo = unidade.getTipo();
            this.situacao = unidade.getSituacao();
            this.unidadeSuperior = unidade.getUnidadeSuperior();
        }
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
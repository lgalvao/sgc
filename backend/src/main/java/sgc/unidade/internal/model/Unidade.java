package sgc.unidade.internal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import sgc.comum.model.EntidadeBase;
import sgc.processo.internal.model.Processo;
import sgc.sgrh.internal.model.Usuario;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Immutable
@Table(name = "VW_UNIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverrides({@AttributeOverride(name = "codigo", column = @Column(name = "codigo"))})
public class Unidade extends EntidadeBase {
    @Column(name = "nome")
    private String nome;

    @Column(name = "sigla", length = 20)
    private String sigla;

    @Column(name = "matricula_titular", length = 8)
    private String matriculaTitular;

    @Column(name = "titulo_titular", length = 12)
    private String tituloTitular;

    @Column(name = "data_inicio_titularidade")
    private LocalDateTime dataInicioTitularidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20)
    private TipoUnidade tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", length = 20)
    private SituacaoUnidade situacao;

    @ManyToOne
    @JoinColumn(name = "unidade_superior_codigo")
    private Unidade unidadeSuperior;

    @ManyToMany(mappedBy = "participantes")
    @Builder.Default
    private Set<Processo> processos = new HashSet<>();

    /**
     * Método temporário para testes. Como Unidade agora é VIEW imutável,
     * este setter apenas atualiza o campo em memória para testes unitários.
     */
    @Deprecated
    public void setTitular(Usuario usuario) {
        if (usuario != null) {
            this.tituloTitular = usuario.getTituloEleitoral();
            this.matriculaTitular = usuario.getMatricula();
        }
    }

    public Unidade(String nome, String sigla) {
        super();
        this.nome = nome;
        this.sigla = sigla;
        this.situacao = SituacaoUnidade.ATIVA;
        this.tipo = TipoUnidade.OPERACIONAL;
    }
}

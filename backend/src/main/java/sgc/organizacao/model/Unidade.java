package sgc.organizacao.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.jspecify.annotations.Nullable;
import sgc.comum.model.EntidadeBase;
import sgc.processo.model.Processo;

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
public class Unidade extends EntidadeBase {
    @Column(name = "nome")
    private String nome;

    @Column(name = "sigla", length = 20, nullable = false)
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
    @Nullable
    private Unidade unidadeSuperior;

    @OneToMany(mappedBy = "unidadeSuperior")
    @Builder.Default
    private java.util.List<Unidade> subunidades = new java.util.ArrayList<>();

    public java.util.List<Unidade> getSubunidades() {
        return subunidades;
    }

    public @Nullable Unidade getUnidadeSuperior() {
        return unidadeSuperior;
    }

    @ManyToMany(mappedBy = "participantes")
    @Builder.Default
    private Set<Processo> processos = new HashSet<>();

    public Unidade(String nome, String sigla) {
        super();
        this.nome = nome;
        this.sigla = sigla;
        this.situacao = SituacaoUnidade.ATIVA;
        this.tipo = TipoUnidade.OPERACIONAL;
    }
}

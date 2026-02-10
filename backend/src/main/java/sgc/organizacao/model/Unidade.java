package sgc.organizacao.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;
import org.jspecify.annotations.Nullable;
import sgc.comum.model.EntidadeBase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Immutable
@Table(name = "VW_UNIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Unidade extends EntidadeBase {
    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "sigla", length = 20, nullable = false)
    private String sigla;

    @Column(name = "matricula_titular", length = 8, nullable = false)
    private String matriculaTitular;

    @Column(name = "titulo_titular", length = 12, nullable = false)
    private String tituloTitular;

    @Column(name = "data_inicio_titularidade", nullable = false)
    private LocalDateTime dataInicioTitularidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20, nullable = false)
    private TipoUnidade tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", length = 20, nullable = false)
    private SituacaoUnidade situacao;

    @ManyToOne
    @JoinColumn(name = "unidade_superior_codigo")
    @Nullable
    private Unidade unidadeSuperior;

    @OneToMany(mappedBy = "unidadeSuperior")
    @Builder.Default
    private List<Unidade> subunidades = new ArrayList<>();

}

package sgc.organizacao.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import org.hibernate.annotations.*;
import org.jspecify.annotations.*;
import sgc.comum.model.*;

import java.time.*;
import java.util.*;

@Entity
@Immutable
@Table(name = "VW_UNIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Unidade extends EntidadeBase {
    @Column(name = "nome", nullable = false)
    @JsonView(ComumViews.Publica.class)
    private String nome;

    @Column(name = "sigla", length = 20, nullable = false)
    @JsonView(ComumViews.Publica.class)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_superior_codigo")
    @Nullable
    @JsonIgnore
    private Unidade unidadeSuperior;

    @OneToMany(mappedBy = "unidadeSuperior", fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Unidade> subunidades = new ArrayList<>();

    @OneToOne(mappedBy = "unidade")
    @Nullable
    @JsonIgnore
    private Responsabilidade responsabilidade;

}

package sgc.mapa.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import org.jspecify.annotations.*;
import sgc.comum.model.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "MAPA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Mapa extends EntidadeBase {
    @JsonView(MapaViews.Publica.class)
    @Column(name = "data_hora_disponibilizado")
    private @Nullable LocalDateTime dataHoraDisponibilizado;

    @JsonView(MapaViews.Publica.class)
    @Column(name = "observacoes_disponibilizacao", length = 1000)
    private @Nullable String observacoesDisponibilizacao;

    @JsonView(MapaViews.Publica.class)
    @Column(name = "sugestoes", length = 1000)
    @Nullable
    private String sugestoes;

    @JsonView(MapaViews.Publica.class)
    @Column(name = "data_hora_homologado")
    private @Nullable LocalDateTime dataHoraHomologado;

    @OneToOne
    @JoinColumn(name = "subprocesso_codigo", nullable = false)
    @JsonIgnore
    private Subprocesso subprocesso;

    @OneToMany(mappedBy = "mapa", cascade = CascadeType.ALL, orphanRemoval = true)
    @lombok.Builder.Default
    @JsonView(MapaViews.Publica.class)
    @JsonIgnoreProperties("mapa")
    private Set<Atividade> atividades = new LinkedHashSet<>();

    @OneToMany(mappedBy = "mapa", cascade = CascadeType.ALL, orphanRemoval = true)
    @lombok.Builder.Default
    @JsonView(MapaViews.Publica.class)
    @JsonIgnoreProperties("mapa")
    private Set<Competencia> competencias = new LinkedHashSet<>();
}

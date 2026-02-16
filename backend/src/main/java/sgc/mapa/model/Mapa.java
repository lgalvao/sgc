package sgc.mapa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;
import sgc.comum.model.EntidadeBase;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

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
    private Set<Atividade> atividades = new LinkedHashSet<>();
}

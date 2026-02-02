package sgc.mapa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;
import sgc.comum.model.EntidadeBase;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;

@Entity
@Table(name = "MAPA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Mapa extends EntidadeBase {
    @Column(name = "data_hora_disponibilizado")
    private @Nullable LocalDateTime dataHoraDisponibilizado;

    @Column(name = "observacoes_disponibilizacao", length = 1000)
    private @Nullable String observacoesDisponibilizacao;

    @Column(name = "sugestoes", length = 1000)
    @Nullable
    private String sugestoes;

    @Column(name = "data_hora_homologado")
    private @Nullable LocalDateTime dataHoraHomologado;

    @OneToOne
    @JoinColumn(name = "subprocesso_codigo", nullable = false)
    private Subprocesso subprocesso;
}

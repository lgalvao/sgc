package sgc.mapa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.model.EntidadeBase;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;

@Entity
@Table(name = "UNIDADE_MAPA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnidadeMapa extends EntidadeBase {
    @Column(name = "data_vigencia")
    private LocalDateTime dataVigencia;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_codigo", unique = true, nullable = false)
    private Unidade unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mapa_vigente_codigo", nullable = false)
    private Mapa mapaVigente;

    public UnidadeMapa(Unidade unidade) {
        this.unidade = unidade;
    }
}

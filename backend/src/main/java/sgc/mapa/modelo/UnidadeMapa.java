package sgc.mapa.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.modelo.EntidadeBase;
import sgc.unidade.modelo.Unidade;

import java.time.LocalDateTime;

/**
 * Entidade que registra o mapa de competências vigente de cada unidade.
 * Uma unidade pode ter apenas um mapa vigente por vez.
 * Quando um novo processo é finalizado, o mapa anterior é substituído pelo novo.
 */
@Entity
@Table(name = "UNIDADE_MAPA", schema = "sgc",
       uniqueConstraints = @UniqueConstraint(columnNames = "unidade_codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnidadeMapa extends EntidadeBase {
    @Column(name = "unidade_codigo", unique = true, nullable = false)
    private Long unidadeCodigo;

    @Column(name = "mapa_vigente_codigo", nullable = false)
    private Long mapaVigenteCodigo;

    @Column(name = "data_vigencia")
    private LocalDateTime dataVigencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_codigo", insertable = false, updatable = false)
    private Unidade unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mapa_vigente_codigo", insertable = false, updatable = false)
    private Mapa mapaVigente;

    /**
     * Construtor de conveniência para criar uma nova associação entre Unidade e Mapa.
     * @param unidadeCodigo O código da unidade.
     */
    public UnidadeMapa(Long unidadeCodigo) {
        this.unidadeCodigo = unidadeCodigo;
    }
}
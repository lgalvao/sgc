package sgc.mapa;

import jakarta.persistence.*;
import lombok.*;
import sgc.comum.BaseEntity;
import sgc.unidade.Unidade;

import java.time.LocalDate;

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
public class UnidadeMapa extends BaseEntity {
    @Column(name = "unidade_codigo", unique = true, nullable = false)
    private Long unidadeCodigo;

    @Column(name = "mapa_vigente_codigo", nullable = false)
    private Long mapaVigenteCodigo;

    @Column(name = "data_vigencia")
    private LocalDate dataVigencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_codigo", insertable = false, updatable = false)
    private Unidade unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mapa_vigente_codigo", insertable = false, updatable = false)
    private Mapa mapaVigente;
}
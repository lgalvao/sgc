package sesel.sgc.processounidade;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import sesel.sgc.EntidadeBase;
import sesel.sgc.mapa.MapaUnidadeSituacao;
import sesel.sgc.mapa.atividades.Atividade;
import sesel.sgc.processo.Processo;
import sesel.sgc.unidade.Unidade;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "processo_unidade",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_processoUnidade_processo_unidade",
                columnNames = {"processo_id", "unidade_id"}
        )
)
@Getter
@Setter
public class ProcessoUnidade extends EntidadeBase {
    @NotNull(message = "Unidade é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "unidade_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_processoUnidade_unidade")
    )
    private Unidade unidade;

    @NotNull(message = "Processo é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "processo_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_processoUnidade_processo")
    )
    private Processo processo;

    @OneToMany(mappedBy = "processoUnidade", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Atividade> atividades;

    @Enumerated(EnumType.STRING)
    @Column
    private MapaUnidadeSituacao situacao = MapaUnidadeSituacao.NAO_INICIADA;

    @Column(name = "datahora_inicio_registro")
    private LocalDateTime dataHoraInicioRegistro;

    @Column(name = "datahora_fim_registro")
    private LocalDateTime dataHoraFimRegistro;

    @Column(name = "datahora_validacao")
    private LocalDateTime dataHoraValidacao;

    @Column(name = "datahora_homologacao")
    private LocalDateTime dataHoraHomologacao;
}

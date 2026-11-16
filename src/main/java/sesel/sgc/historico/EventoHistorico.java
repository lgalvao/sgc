package sesel.sgc.historico;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import sesel.sgc.EntidadeBase;
import sesel.sgc.unidade.Unidade;

import java.time.LocalDateTime;

@Entity
@Table(name = "evento_historico")
@Getter
@Setter
public class EventoHistorico extends EntidadeBase {
    @Enumerated(EnumType.STRING)
    private EventoHistoricoTipo tipo;

    @Column(name = "datahora")
    private LocalDateTime dataHora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "unidade_id",
            foreignKey = @ForeignKey(name = "fk_evento_historico_unidade")
    )
    private Unidade unidade;

    @NotNull(message = "Histórico é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "historico_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_evento_historico_historico")
    )
    private Historico historico;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String detalhes;
}

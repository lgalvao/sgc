package sesel.sgc.atribuicao;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import sesel.sgc.EntidadeBase;
import sesel.sgc.processo.Processo;
import sesel.sgc.servidor.Servidor;
import sesel.sgc.unidade.Unidade;

import java.time.LocalDate;

/**
 * Representa uma atribuição de um processo a um servidor em uma unidade.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "atribuicao",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_atribuicao_servidor_processo",
                columnNames = {"servidor_id", "processo_id"}
        )
)
@Getter
@Setter
public class Atribuicao extends EntidadeBase {
    @NotNull(message = "Unidade é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "unidade_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_atribuicao_unidade")
    )
    private Unidade unidade;

    @NotNull(message = "Processo é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "processo_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_atribuicao_processo")
    )
    private Processo processo;

    @NotNull(message = "Servidor é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "servidor_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_atribuicao_servidor")
    )
    private Servidor servidor;

    @NotNull(message = "Data de início é obrigatória")
    @FutureOrPresent(message = "Data de início deve ser atual ou futura")
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @FutureOrPresent(message = "Data de término deve ser atual ou futura")
    @Column(name = "data_termino")
    private LocalDate dataTermino;

    @Column(length = 500)
    private String justificativa;
}

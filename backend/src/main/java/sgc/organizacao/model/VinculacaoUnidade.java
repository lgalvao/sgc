package sgc.organizacao.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import org.hibernate.annotations.*;
import org.jspecify.annotations.*;

/**
 * Representa a vinculação entre uma unidade atual e sua unidade anterior (histórico).
 *
 * <p>Esta entidade é uma view imutável que mapeia o histórico de códigos de unidades.
 * Para unidades raiz (que nunca foram renomeadas), {@code unidadeAnteriorCodigo} será null.
 */
@Entity
@Immutable
@Table(name = "VW_VINCULACAO_UNIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class VinculacaoUnidade {
    @Id
    @Column(name = "unidade_atual_codigo", nullable = false)
    private Long unidadeAtualCodigo;

    @Column(name = "unidade_anterior_codigo")
    private @Nullable Long unidadeAnteriorCodigo;

    @Column(name = "demais_unidades_historicas")
    private @Nullable String demaisUnidadesHistoricas;
}

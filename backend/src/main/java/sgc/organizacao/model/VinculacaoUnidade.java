package sgc.organizacao.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "VW_VINCULACAO_UNIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@IdClass(VinculacaoUnidadeId.class)
public class VinculacaoUnidade {
    @Id
    @Column(name = "unidade_atual_codigo", nullable = false)
    private Long unidadeAtualCodigo;

    @Id
    @Column(name = "unidade_anterior_codigo", nullable = false)
    private Long unidadeAnteriorCodigo;

    @Column(name = "demais_unidades_historicas")
    private String demaisUnidadesHistoricas;
}

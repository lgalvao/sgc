package sgc.unidade.internal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "VW_VINCULACAO_UNIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(VinculacaoUnidadeId.class)
public class VinculacaoUnidade {
    @Id
    @Column(name = "unidade_atual_codigo")
    private Long unidadeAtualCodigo;

    @Id
    @Column(name = "unidade_anterior_codigo")
    private Long unidadeAnteriorCodigo;

    @Column(name = "demais_unidades_historicas")
    private String demaisUnidadesHistoricas;
}

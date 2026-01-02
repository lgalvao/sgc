package sgc.organizacao.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VinculacaoUnidadeId implements Serializable {
    private Long unidadeAtualCodigo;
    private Long unidadeAnteriorCodigo;
}

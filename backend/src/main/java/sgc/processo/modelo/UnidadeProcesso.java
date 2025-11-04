package sgc.processo.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.modelo.EntidadeBase;
import sgc.unidade.modelo.TipoUnidade;

@Entity
@Table(name = "UNIDADE_PROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnidadeProcesso extends EntidadeBase {
    @Column(name = "processo_codigo")
    private Long codProcesso;

    @Column(name = "unidade_codigo")
    private Long codUnidade;

    @Column(name = "nome")
    private String nome;

    @Column(name = "sigla", length = 20)
    private String sigla;

    @Column(name = "titular_titulo", length = 12)
    private String titularTitulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20)
    private TipoUnidade tipo;

    @Column(name = "situacao", length = 20)
    private String situacao;

    @Column(name = "unidade_superior_codigo")
    private Long codUnidadeSuperior;
}
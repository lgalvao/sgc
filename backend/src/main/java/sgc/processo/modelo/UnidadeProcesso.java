package sgc.processo.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.modelo.EntidadeBase;

@Entity
@Table(name = "UNIDADE_PROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnidadeProcesso extends EntidadeBase {
    @Column(name = "processo_codigo")
    private Long processoCodigo;

    @Column(name = "nome")
    private String nome;

    @Column(name = "sigla", length = 20)
    private String sigla;

    @Column(name = "titular_titulo", length = 12)
    private String titularTitulo;

    @Column(name = "tipo", length = 20)
    private String tipo;

    @Column(name = "situacao", length = 20)
    private String situacao;

    @Column(name = "unidade_superior_codigo")
    private Long unidadeSuperiorCodigo;
}
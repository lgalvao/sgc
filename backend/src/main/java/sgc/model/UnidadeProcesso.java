package sgc.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "UNIDADE_PROCESSO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnidadeProcesso implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Long codigo;

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
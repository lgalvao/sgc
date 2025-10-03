package sgc.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "UNIDADE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Unidade implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Long codigo;

    @Column(name = "nome")
    private String nome;

    @Column(name = "sigla", length = 20)
    private String sigla;

    @ManyToOne
    @JoinColumn(name = "titular_titulo")
    private Usuario titular;

    @Column(name = "tipo", length = 20)
    private String tipo;

    @Column(name = "situacao", length = 20)
    private String situacao;

    @ManyToOne
    @JoinColumn(name = "unidade_superior_codigo")
    private Unidade unidadeSuperior;
}
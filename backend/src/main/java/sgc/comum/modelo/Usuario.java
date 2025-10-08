package sgc.comum.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.unidade.modelo.Unidade;

import java.io.Serializable;

@Entity
@Table(name = "USUARIO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements Serializable {
    @Id
    @Column(name = "titulo", length = 12)
    private String titulo;

    @Column(name = "nome")
    private String nome;

    @Column(name = "email")
    private String email;

    @Column(name = "ramal", length = 20)
    private String ramal;

    @ManyToOne
    @JoinColumn(name = "unidade_codigo")
    private Unidade unidade;

}
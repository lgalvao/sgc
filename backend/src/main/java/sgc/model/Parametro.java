package sgc.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "PARAMETRO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Parametro implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Long codigo;

    @Column(name = "chave", length = 50)
    private String chave;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "valor")
    private String valor;
}
package sgc.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "VINCULACAO_UNIDADE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VinculacaoUnidade implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Long codigo;

    @ManyToOne
    @JoinColumn(name = "unidade_anterior_codigo")
    private Unidade unidadeAnterior;

    @ManyToOne
    @JoinColumn(name = "unidade_atual_codigo")
    private Unidade unidadeAtual;
}
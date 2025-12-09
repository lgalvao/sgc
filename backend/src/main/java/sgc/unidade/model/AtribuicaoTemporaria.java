package sgc.unidade.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.model.EntidadeBase;
import sgc.sgrh.model.Usuario;

import java.time.LocalDateTime;

/**
 * Representa a atribuição temporária de um usuário a uma unidade, por exemplo, para cobrir férias
 * ou licenças.
 */
@Entity
@Table(name = "ATRIBUICAO_TEMPORARIA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AtribuicaoTemporaria extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "unidade_codigo")
    private Unidade unidade;

    @ManyToOne
    @JoinColumn(name = "usuario_titulo")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil")
    private sgc.sgrh.model.Perfil perfil;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_termino")
    private LocalDateTime dataTermino;

    @Column(name = "justificativa", length = 500)
    private String justificativa;
}

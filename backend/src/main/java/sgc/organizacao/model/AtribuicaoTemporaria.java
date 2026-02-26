package sgc.organizacao.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import sgc.comum.model.*;

import java.time.*;

/**
 * Representa a atribuição temporária de um usuário a uma unidade, por exemplo, para cobrir férias
 * ou licenças.
 */
@Entity
@Table(name = "ATRIBUICAO_TEMPORARIA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class AtribuicaoTemporaria extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "unidade_codigo", nullable = false)
    private Unidade unidade;

    @Column(name = "usuario_titulo", length = 12, nullable = false)
    private String usuarioTitulo;

    @Column(name = "usuario_matricula", length = 8, nullable = false)
    private String usuarioMatricula;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_termino", nullable = false)
    private LocalDateTime dataTermino;

    @Column(name = "justificativa", length = 500)
    private String justificativa;

    @ManyToOne
    @JoinColumn(name = "usuario_titulo", insertable = false, updatable = false, nullable = false)
    private Usuario usuario;

    @Transient
    private Perfil perfil;
}

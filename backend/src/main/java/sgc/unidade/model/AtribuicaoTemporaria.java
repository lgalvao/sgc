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

    @Column(name = "usuario_titulo", length = 12)
    private String usuarioTitulo;

    @Column(name = "usuario_matricula", length = 8)
    private String usuarioMatricula;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_termino")
    private LocalDateTime dataTermino;

    @Column(name = "justificativa", length = 500)
    private String justificativa;

    @ManyToOne
    @JoinColumn(name = "usuario_titulo", insertable = false, updatable = false)
    private Usuario usuario;

    @Transient
    private sgc.sgrh.model.Perfil perfil;

    public sgc.sgrh.model.Perfil getPerfil() {
        return perfil != null ? perfil : sgc.sgrh.model.Perfil.SERVIDOR;
    }

    public void setPerfil(sgc.sgrh.model.Perfil perfil) {
        this.perfil = perfil;
    }
}

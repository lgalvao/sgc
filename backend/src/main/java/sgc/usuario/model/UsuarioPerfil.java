package sgc.usuario.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import sgc.unidade.model.Unidade;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Immutable
@Table(name = "VW_USUARIO_PERFIL_UNIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UsuarioPerfilId.class)
public class UsuarioPerfil implements Serializable {
    @Id
    @Column(name = "usuario_titulo", length = 12)
    private String usuarioTitulo;

    @Id
    @Column(name = "unidade_codigo")
    private Long unidadeCodigo;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "perfil")
    private Perfil perfil;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_titulo", insertable = false, updatable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unidade_codigo", insertable = false, updatable = false)
    private Unidade unidade;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UsuarioPerfil that = (UsuarioPerfil) obj;
        return Objects.equals(usuarioTitulo, that.usuarioTitulo)
                && Objects.equals(unidadeCodigo, that.unidadeCodigo)
                && perfil == that.perfil;
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuarioTitulo, unidadeCodigo, perfil);
    }
}

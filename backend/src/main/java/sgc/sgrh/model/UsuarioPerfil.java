package sgc.sgrh.model;

import jakarta.persistence.*;
import lombok.*;
import sgc.unidade.model.Unidade;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "USUARIO_PERFIL", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioPerfil implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_titulo_eleitoral")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unidade_codigo")
    private Unidade unidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil")
    private Perfil perfil;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioPerfil that = (UsuarioPerfil) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

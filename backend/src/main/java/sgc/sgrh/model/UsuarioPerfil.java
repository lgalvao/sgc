package sgc.sgrh.model;

import jakarta.persistence.*;
import lombok.*;
import sgc.unidade.model.Unidade;

import java.io.Serializable;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_titulo_eleitoral")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unidade_codigo")
    private Unidade unidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil")
    private Perfil perfil;
}

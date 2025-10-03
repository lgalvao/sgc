package sgc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "ADMINISTRADOR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Administrador implements Serializable {
    @Id
    @Column(name = "usuario_titulo", length = 12)
    private String usuarioTitulo;

    @OneToOne
    @MapsId
    @JoinColumn(name = "usuario_titulo")
    private Usuario usuario;

}
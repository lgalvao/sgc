package sgc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "ALERTA_USUARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertaUsuario implements Serializable {
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        @Column(name = "alerta_codigo")
        private Long alertaCodigo;
    
        @Column(name = "usuario_titulo", length = 12)
        private String usuarioTitulo;
    
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Id id = (Id) o;
            return Objects.equals(alertaCodigo, id.alertaCodigo) &&
                   Objects.equals(usuarioTitulo, id.usuarioTitulo);
        }
    
        @Override
        public int hashCode() {
            return Objects.hash(alertaCodigo, usuarioTitulo);
        }
    }

    @EmbeddedId
    private Id id;

    @MapsId("alertaCodigo")
    @ManyToOne
    @JoinColumn(name = "alerta_codigo", insertable = false, updatable = false)
    private Alerta alerta;

    @MapsId("usuarioTitulo")
    @ManyToOne
    @JoinColumn(name = "usuario_titulo", insertable = false, updatable = false)
    private Usuario usuario;

    @Column(name = "data_hora_leitura")
    private LocalDateTime dataHoraLeitura;
}
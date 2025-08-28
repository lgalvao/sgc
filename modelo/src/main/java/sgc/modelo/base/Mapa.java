package sgc.modelo.base;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "MAPA")
public class Mapa extends EntidadeBase {
    @ManyToOne
    Subprocesso subprocesso;

    LocalDateTime dataHoraDisponibilizado;
    LocalDateTime dataHoraHomologado;
}
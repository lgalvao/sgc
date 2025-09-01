package sgc.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "MAPA")
public class Mapa extends EntidadeBase {
    LocalDateTime dataHoraDisponibilizado;
    LocalDateTime dataHoraHomologado;
}
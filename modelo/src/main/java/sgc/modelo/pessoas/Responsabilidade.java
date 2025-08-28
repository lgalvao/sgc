package sgc.modelo.pessoas;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sgc.modelo.base.EntidadeBase;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "responsabilidade")
public class Responsabilidade extends EntidadeBase {
    @ManyToOne
    Usuario usuario;

    @Enumerated(EnumType.STRING)
    TipoResponsabilidade tipo;

    LocalDateTime inicio;
    LocalDateTime fim;
}
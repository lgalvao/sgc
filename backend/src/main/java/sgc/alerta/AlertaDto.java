package sgc.alerta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertaDto {
    private Long codigo;
    private Long processoCodigo;
    private String descricao;
    private LocalDateTime dataHora;
    private Long unidadeOrigemCodigo;
    private Long unidadeDestinoCodigo;
    private String usuarioDestinoTitulo;
}
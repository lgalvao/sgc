package sgc.processo.eventos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento de domínio publicado quando um Processo é finalizado.
 *
 * <p>Este evento sinaliza que todos os subprocessos foram homologados, os mapas foram tornados
 * vigentes e as notificações de conclusão foram enviadas. Pode ser usado para acionar lógicas de
 * pós-processamento, como arquivamento ou geração de relatórios consolidados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoProcessoFinalizado {
    private Long codProcesso;
    private LocalDateTime dataHoraFinalizacao;
}

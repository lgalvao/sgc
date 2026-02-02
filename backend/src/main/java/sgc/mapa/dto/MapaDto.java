package sgc.mapa.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO para operações CRUD simples de Mapa.
 *
 * <p>Inclui metadados do ciclo de vida do mapa como datas de disponibilização e homologação, além
 * de observações e sugestões. Usado primariamente nos endpoints REST do {@link
 * sgc.mapa.MapaController}.
 *
 * <p>Para operações que manipulam a composição completa do mapa (competências e atividades
 * vinculadas), use {@link MapaCompletoDto}, que oferece uma estrutura hierárquica mais apropriada
 * para esse contexto.
 */
@Builder
public record MapaDto(
        Long codigo,
        LocalDateTime dataHoraDisponibilizado,
        String observacoesDisponibilizacao,
        LocalDateTime dataHoraHomologado,
        String sugestoes) {
}

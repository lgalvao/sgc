package sgc.mapa.dto;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * DTO que representa um mapa completo com todas as suas competências e os vínculos com atividades
 * aninhados.
 *
 * <p>Usado para operações agregadas de leitura e escrita do mapa no contexto de subprocessos.
 *
 * <p>Diferente de {@link sgc.mapa.dto.MapaDto}, que é usado para operações CRUD simples do mapa e
 * inclui metadados como datas de disponibilização e homologação. Este DTO se foca na estrutura
 * hierárquica mapa → competências → atividades, sendo mais adequado para operações que manipulam a
 * composição completa do mapa.
 */
@Builder
public record MapaCompletoDto(
        Long codigo,
        @Nullable Long subprocessoCodigo,
        @Nullable String observacoes,
        List<CompetenciaMapaDto> competencias) {
}

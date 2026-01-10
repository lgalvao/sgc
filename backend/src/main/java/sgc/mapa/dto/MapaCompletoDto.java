package sgc.mapa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapaCompletoDto {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private Long codigo;
    private Long subprocessoCodigo;
    private String observacoes;
    private List<CompetenciaMapaDto> competencias;
}

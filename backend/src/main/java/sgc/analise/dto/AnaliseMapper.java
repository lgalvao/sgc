package sgc.analise.dto;

import org.mapstruct.Mapper;
import sgc.analise.model.Analise;

/**
 * Mapper para converter a entidade {@link Analise} em DTOs.
 */
@Mapper(componentModel = "spring")
public interface AnaliseMapper {
    /**
     * Converte uma entidade {@link Analise} em um DTO {@link AnaliseHistoricoDto}.
     * @param analise A entidade a ser convertida.
     * @return O DTO correspondente.
     */
    AnaliseHistoricoDto toAnaliseHistoricoDto(Analise analise);

    /**
     * Converte uma entidade {@link Analise} em um DTO {@link AnaliseValidacaoHistoricoDto}.
     * @param analise A entidade a ser convertida.
     * @return O DTO correspondente.
     */
    AnaliseValidacaoHistoricoDto toAnaliseValidacaoHistoricoDto(Analise analise);
}

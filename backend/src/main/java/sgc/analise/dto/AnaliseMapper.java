package sgc.analise.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.analise.modelo.Analise;

@Mapper(componentModel = "spring")
public interface AnaliseMapper {

    AnaliseHistoricoDto toAnaliseHistoricoDto(Analise analise);

    AnaliseValidacaoHistoricoDto toAnaliseValidacaoHistoricoDto(Analise analise);
}

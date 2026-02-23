package sgc.mapa.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.comum.config.CentralMapperConfig;
import sgc.mapa.model.Conhecimento;

@Mapper(config = CentralMapperConfig.class)
public interface ConhecimentoMapper {

    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "atividade", ignore = true)
    Conhecimento toEntity(CriarConhecimentoRequest request);

    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "atividade", ignore = true)
    Conhecimento toEntity(AtualizarConhecimentoRequest request);
}

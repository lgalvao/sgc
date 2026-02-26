package sgc.mapa.dto;

import org.mapstruct.*;
import sgc.comum.config.*;
import sgc.mapa.model.*;

@Mapper(config = CentralMapperConfig.class)
public interface ConhecimentoMapper {

    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "atividade", ignore = true)
    Conhecimento toEntity(CriarConhecimentoRequest request);

    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "atividade", ignore = true)
    Conhecimento toEntity(AtualizarConhecimentoRequest request);
}

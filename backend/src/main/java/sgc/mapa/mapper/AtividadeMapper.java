package sgc.mapa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import sgc.comum.config.CentralMapperConfig;
import sgc.mapa.dto.AtividadeResponse;
import sgc.mapa.dto.AtualizarAtividadeRequest;
import sgc.mapa.dto.CriarAtividadeRequest;
import sgc.mapa.model.Atividade;

@SuppressWarnings("NullableProblems")
@Component
@Mapper(config = CentralMapperConfig.class)
public abstract class AtividadeMapper {
    @Mapping(source = "mapa.codigo", target = "mapaCodigo")
    public abstract AtividadeResponse toResponse(Atividade atividade);

    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "mapa", ignore = true)
    @Mapping(target = "conhecimentos", ignore = true)
    @Mapping(target = "competencias", ignore = true)
    public abstract Atividade toEntity(CriarAtividadeRequest request);

    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "mapa", ignore = true)
    @Mapping(target = "conhecimentos", ignore = true)
    @Mapping(target = "competencias", ignore = true)
    public abstract Atividade toEntity(AtualizarAtividadeRequest request);

}

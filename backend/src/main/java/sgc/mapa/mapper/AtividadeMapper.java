package sgc.mapa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import sgc.mapa.dto.AtividadeResponse;
import sgc.mapa.dto.AtualizarAtividadeRequest;
import sgc.mapa.dto.CriarAtividadeRequest;
import sgc.mapa.model.Atividade;

/**
 * Mapper (usando MapStruct) entre a entidade Atividade e seus DTOs.
 */
@Component
@Mapper(componentModel = "spring")
public abstract class AtividadeMapper {

    /**
     * Converte uma entidade {@link Atividade} em um {@link AtividadeResponse}.
     */
    @Mapping(source = "mapa.codigo", target = "mapaCodigo")
    public abstract AtividadeResponse toResponse(Atividade atividade);

    /**
     * Converte um {@link CriarAtividadeRequest} em uma entidade {@link Atividade}.
     */
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "mapa", ignore = true)
    @Mapping(target = "conhecimentos", ignore = true)
    @Mapping(target = "competencias", ignore = true)
    public abstract Atividade toEntity(CriarAtividadeRequest request);

    /**
     * Converte um {@link AtualizarAtividadeRequest} em uma entidade {@link Atividade}.
     */
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "mapa", ignore = true)
    @Mapping(target = "conhecimentos", ignore = true)
    @Mapping(target = "competencias", ignore = true)
    public abstract Atividade toEntity(AtualizarAtividadeRequest request);

}

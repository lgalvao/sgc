package sgc.mapa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import sgc.mapa.dto.AtualizarConhecimentoRequest;
import sgc.mapa.dto.ConhecimentoResponse;
import sgc.mapa.dto.CriarConhecimentoRequest;
import sgc.mapa.model.Conhecimento;

/**
 * Mapper (usando MapStruct) entre a entidade Conhecimento e seus DTOs.
 * 
 * <p>Este mapper é puro e não injeta repositórios. Conversões de IDs para entidades
 * devem ser feitas nos Services antes de chamar o mapper.</p>
 */
@Mapper(componentModel = "spring")
public interface ConhecimentoMapper {
    @Mapping(source = "atividade.codigo", target = "atividadeCodigo")
    ConhecimentoResponse toResponse(Conhecimento conhecimento);

    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "atividade", ignore = true)
    Conhecimento toEntity(CriarConhecimentoRequest request);

    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "atividade", ignore = true)
    Conhecimento toEntity(AtualizarConhecimentoRequest request);
}

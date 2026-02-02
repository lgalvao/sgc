package sgc.subprocesso.mapper;

import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.model.Subprocesso;

/**
 * Mapper (usando MapStruct) entre a entidade Subprocesso e seu DTO.
 * 
 * <p>Este mapper é puro e não injeta repositórios. Conversões de IDs para entidades
 * devem ser feitas nos Services antes de chamar o mapper.</p>
 */
@Mapper(componentModel = "spring")
public interface SubprocessoMapper {
    @Mapping(source = "processo.codigo", target = "codProcesso")
    @Mapping(source = "unidade.codigo", target = "codUnidade")
    @Mapping(source = "mapa.codigo", target = "codMapa")
    SubprocessoDto toDto(@Nullable Subprocesso subprocesso);
}

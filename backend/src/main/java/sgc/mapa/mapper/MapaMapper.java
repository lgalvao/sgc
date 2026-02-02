package sgc.mapa.mapper;

import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import sgc.mapa.dto.MapaDto;
import sgc.mapa.model.Mapa;

/**
 * Mapper (usando MapStruct) entre a entidade Mapa e seu DTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MapaMapper {
    @Nullable MapaDto toDto(@Nullable Mapa mapa);

    @Nullable Mapa toEntity(@Nullable MapaDto mapaDto);
}

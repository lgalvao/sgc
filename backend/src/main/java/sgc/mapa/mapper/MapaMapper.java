package sgc.mapa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import sgc.mapa.dto.MapaDto;
import sgc.mapa.model.Mapa;

/**
 * Mapper (usando MapStruct) entre a entidade Mapa e seu DTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MapaMapper {
    MapaDto toDto(Mapa mapa);

    Mapa toEntity(MapaDto mapaDto);
}

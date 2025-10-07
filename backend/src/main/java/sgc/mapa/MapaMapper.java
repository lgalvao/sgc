package sgc.mapa;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import sgc.mapa.dto.MapaDto;

/**
 * Mapper (usando MapStruct) entre a entidade Mapa e seu DTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MapaMapper {

    MapaDto toDTO(Mapa mapa);

    Mapa toEntity(MapaDto mapaDto);

}

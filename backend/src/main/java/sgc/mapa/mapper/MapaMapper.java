package sgc.mapa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import sgc.comum.config.CentralMapperConfig;
import sgc.mapa.dto.MapaDto;
import sgc.mapa.model.Mapa;

@Mapper(config = CentralMapperConfig.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MapaMapper {
    MapaDto toDto(Mapa mapa);

    Mapa toEntity(MapaDto mapaDto);
}

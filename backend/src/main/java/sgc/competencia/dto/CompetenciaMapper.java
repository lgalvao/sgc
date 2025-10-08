package sgc.competencia.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.competencia.modelo.Competencia;
import sgc.mapa.modelo.Mapa;

/**
 * Mapper (usando MapStruct) entre a entidade Competencia e seu DTO.
 */
@Mapper(componentModel = "spring")
public interface CompetenciaMapper {

    @Mapping(source = "mapa.codigo", target = "mapaCodigo")
    CompetenciaDto toDTO(Competencia competencia);

    @Mapping(source = "mapaCodigo", target = "mapa")
    Competencia toEntity(CompetenciaDto competenciaDTO);

    default Mapa map(Long value) {
        if (value == null) {
            return null;
        }
        Mapa mapa = new Mapa();
        mapa.setCodigo(value);
        return mapa;
    }
}
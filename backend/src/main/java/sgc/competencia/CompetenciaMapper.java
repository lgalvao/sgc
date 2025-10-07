package sgc.competencia;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.mapa.Mapa;

/**
 * Mapper (usando MapStruct) entre a entidade Competencia e seu DTO.
 */
@Mapper(componentModel = "spring")
public interface CompetenciaMapper {

    @Mapping(source = "mapa.codigo", target = "mapaCodigo")
    CompetenciaDTO toDTO(Competencia competencia);

    @Mapping(source = "mapaCodigo", target = "mapa")
    Competencia toEntity(CompetenciaDTO competenciaDTO);

    default Mapa map(Long value) {
        if (value == null) {
            return null;
        }
        Mapa mapa = new Mapa();
        mapa.setCodigo(value);
        return mapa;
    }
}
package sgc.atividade;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.mapa.Mapa;

/**
 * Mapper (usando MapStruct) entre a entidade Atividade e seu DTO.
 */
@Mapper(componentModel = "spring")
public interface AtividadeMapper {

    @Mapping(source = "mapa.codigo", target = "mapaCodigo")
    AtividadeDTO toDTO(Atividade atividade);

    @Mapping(source = "mapaCodigo", target = "mapa")
    Atividade toEntity(AtividadeDTO atividadeDTO);

    default Mapa map(Long value) {
        if (value == null) {
            return null;
        }
        Mapa mapa = new Mapa();
        mapa.setCodigo(value);
        return mapa;
    }
}

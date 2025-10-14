package sgc.atividade.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.atividade.modelo.Atividade;
import sgc.mapa.modelo.Mapa;

/**
 * Mapper (usando MapStruct) entre a entidade Atividade e seu DTO.
 */
@Mapper(componentModel = "spring")
public interface AtividadeMapper {
    @Mapping(source = "mapa.codigo", target = "mapaCodigo")
    AtividadeDto toDTO(Atividade atividade);

    @Mapping(source = "mapaCodigo", target = "mapa")
    @Mapping(target = "conhecimentos", ignore = true)
    Atividade toEntity(AtividadeDto atividadeDTO);

    default Mapa map(Long value) {
        if (value == null) return null;

        Mapa mapa = new Mapa();
        mapa.setCodigo(value);
        return mapa;
    }
}

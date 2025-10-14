package sgc.atividade.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.atividade.modelo.Atividade;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mapper (usando MapStruct) entre a entidade Atividade e seu DTO.
 */
@Component
@Mapper(componentModel = "spring")
public abstract class AtividadeMapper {
    @Autowired
    protected MapaRepo mapaRepo;

    @Mapping(source = "mapa.codigo", target = "mapaCodigo")
    public abstract AtividadeDto toDTO(Atividade atividade);

    @Mapping(source = "mapaCodigo", target = "mapa")
    @Mapping(target = "conhecimentos", ignore = true)
    public abstract Atividade toEntity(AtividadeDto atividadeDTO);

    public Mapa map(Long value) {
        if (value == null) {
            return null;
        }
        return mapaRepo.findById(value)
                .orElseThrow(() -> new RuntimeException("Mapa não encontrado com o código: " + value));
    }
}
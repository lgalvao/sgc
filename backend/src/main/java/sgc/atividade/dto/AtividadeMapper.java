package sgc.atividade.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sgc.atividade.modelo.Atividade;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;

/**
 * Mapper (usando MapStruct) entre a entidade Atividade e seu DTO.
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
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

    public Mapa map(Long idMapa) {
        if (idMapa == null) return null;
        return mapaRepo.findById(idMapa).orElseThrow(() -> new ErroDominioNaoEncontrado("Mapa", idMapa));
    }
}
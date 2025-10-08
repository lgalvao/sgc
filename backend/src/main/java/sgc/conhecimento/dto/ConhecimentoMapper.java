package sgc.conhecimento.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.atividade.modelo.Atividade;
import sgc.conhecimento.modelo.Conhecimento;

/**
 * Mapper (usando MapStruct) entre a entidade Conhecimento e seu DTO.
 */
@Mapper(componentModel = "spring")
public interface ConhecimentoMapper {

    @Mapping(source = "atividade.codigo", target = "atividadeCodigo")
    ConhecimentoDto toDTO(Conhecimento conhecimento);

    @Mapping(source = "atividadeCodigo", target = "atividade")
    Conhecimento toEntity(ConhecimentoDto conhecimentoDTO);

    default Atividade map(Long value) {
        if (value == null) {
            return null;
        }
        Atividade atividade = new Atividade();
        atividade.setCodigo(value);
        return atividade;
    }
}

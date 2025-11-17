package sgc.processo.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.model.Processo;

/**
 * Mapper (usando MapStruct) entre a entidade Processo e seu DTO principal.
 */
@Mapper(componentModel = "spring")
public interface ProcessoMapper {
    ProcessoDto toDto(Processo processo);

    @Mapping(target = "participantes", ignore = true)
    Processo toEntity(ProcessoDto processoDTO);
}

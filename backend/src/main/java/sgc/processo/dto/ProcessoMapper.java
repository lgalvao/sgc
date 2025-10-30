package sgc.processo.dto;

import org.mapstruct.Mapper;
import sgc.processo.modelo.Processo;

/**
 * Mapper (usando MapStruct) entre a entidade Processo e seu DTO principal.
 */
@Mapper(componentModel = "spring")
public interface ProcessoMapper {
    ProcessoDto toDto(Processo processo);

    Processo toEntity(ProcessoDto processoDTO);
}

package sgc.processo;

import org.mapstruct.Mapper;
import sgc.processo.dto.ProcessoDTO;

/**
 * Mapper (usando MapStruct) entre a entidade Processo e seu DTO principal.
 */
@Mapper(componentModel = "spring")
public interface ProcessoMapper {

    ProcessoDTO toDTO(Processo processo);

    Processo toEntity(ProcessoDTO processoDTO);

}

package sgc.processo.mapper;

import sgc.comum.config.CentralMapperConfig;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.model.Processo;

import java.time.format.DateTimeFormatter;

/**
 * Mapper (usando MapStruct) entre a entidade Processo e seu DTO principal.
 */
@Mapper(componentModel = "spring", config = CentralMapperConfig.class)
public interface ProcessoMapper {
    @Mapping(target = "unidadesParticipantes", expression = "java(processo.getSiglasParticipantes())")
    ProcessoDto toDto(Processo processo);

    @Mapping(target = "participantes", ignore = true)
    Processo toEntity(ProcessoDto processoDTO);
}

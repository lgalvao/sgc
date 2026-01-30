package sgc.processo.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.model.Processo;

import java.util.stream.Collectors;
import sgc.organizacao.model.Unidade;

/**
 * Mapper (usando MapStruct) entre a entidade Processo e seu DTO principal.
 */
@Mapper(componentModel = "spring")
public interface ProcessoMapper {
    @Mapping(target = "dataCriacaoFormatada", ignore = true)
    @Mapping(target = "dataFinalizacaoFormatada", ignore = true)
    @Mapping(target = "dataLimiteFormatada", ignore = true)
    @Mapping(target = "situacaoLabel", ignore = true)
    @Mapping(target = "tipoLabel", ignore = true)
    @Mapping(target = "unidadesParticipantes", ignore = true)
    ProcessoDto toDto(Processo processo);

    @Mapping(target = "participantes", ignore = true)
    Processo toEntity(ProcessoDto processoDTO);

    @AfterMapping
    default void mapUnidadesParticipantes(Processo processo, @MappingTarget ProcessoDto dto) {
        if (processo.getParticipantes() != null) {
            String siglas = processo.getParticipantes().stream()
                    .map(Unidade::getSigla)
                    .sorted()
                    .collect(Collectors.joining(", "));
            dto.setUnidadesParticipantes(siglas);
        }
    }
}


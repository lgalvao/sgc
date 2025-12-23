package sgc.processo.dto.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sgc.comum.util.FormatadorData;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.model.Processo;
import sgc.unidade.internal.model.Unidade;

import java.util.stream.Collectors;

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

    /**
     * Popula campos formatados após o mapeamento básico.
     */
    @AfterMapping
    default void populaCamposFormatados(Processo processo, @MappingTarget ProcessoDto dto) {
        dto.setDataCriacaoFormatada(FormatadorData.formatarData(processo.getDataCriacao()));
        dto.setDataFinalizacaoFormatada(
                FormatadorData.formatarData(processo.getDataFinalizacao()));
        dto.setDataLimiteFormatada(FormatadorData.formatarData(processo.getDataLimite()));
        dto.setSituacaoLabel(processo.getSituacao().getLabel());
        dto.setTipoLabel(processo.getTipo().getLabel());

        if (processo.getParticipantes() != null && !processo.getParticipantes().isEmpty()) {
            String participantes = processo.getParticipantes().stream()
                    .map(Unidade::getSigla)
                    .sorted()
                    .collect(Collectors.joining(", "));
            dto.setUnidadesParticipantes(participantes);
        } else {
            dto.setUnidadesParticipantes(null);
        }
    }
}

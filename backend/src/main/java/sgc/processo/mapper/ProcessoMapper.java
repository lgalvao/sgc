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
    default void mapAfterMapping(Processo processo, @MappingTarget ProcessoDto dto) {
        if (processo.getParticipantes() != null) {
            dto.setUnidadesParticipantes(processo.getSiglasParticipantes());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        if (processo.getDataCriacao() != null) {
            dto.setDataCriacaoFormatada(processo.getDataCriacao().format(formatter));
        }
        if (processo.getDataFinalizacao() != null) {
            dto.setDataFinalizacaoFormatada(processo.getDataFinalizacao().format(formatter));
        }
        if (processo.getDataLimite() != null) {
            dto.setDataLimiteFormatada(processo.getDataLimite().format(formatter));
        }
        if (processo.getSituacao() != null) {
            dto.setSituacaoLabel(processo.getSituacao().getLabel());
        }
        if (processo.getTipo() != null) {
            dto.setTipoLabel(processo.getTipo().getLabel());
        }
    }
}

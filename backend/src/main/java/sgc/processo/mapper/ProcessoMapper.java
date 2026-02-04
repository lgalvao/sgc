package sgc.processo.mapper;

import org.jspecify.annotations.Nullable;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.model.Processo;

import java.time.format.DateTimeFormatter;
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
    @Nullable ProcessoDto toDto(@Nullable Processo processo);

    @Mapping(target = "participantes", ignore = true)
    @Nullable Processo toEntity(@Nullable ProcessoDto processoDTO);

    @AfterMapping
    default void mapAfterMapping(Processo processo, @MappingTarget ProcessoDto dto) {
        if (processo.getParticipantes() != null) {
            String siglas = processo.getParticipantes().stream()
                    .map(Unidade::getSigla)
                    .sorted()
                    .collect(Collectors.joining(", "));
            dto.setUnidadesParticipantes(siglas);
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


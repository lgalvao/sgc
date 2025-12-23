package sgc.mapa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MapaCompletoMapper {
    default MapaCompletoDto toDto(Mapa mapa, Long codSubprocesso, List<Competencia> competencias) {
        return MapaCompletoDto.builder()
                .codigo(mapa == null ? null : mapa.getCodigo())
                .subprocessoCodigo(codSubprocesso)
                .observacoes(mapa == null ? null : mapa.getObservacoesDisponibilizacao())
                .competencias(competencias == null ? null : competencias.stream().map(this::toDto).toList())
                .build();
    }

    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "descricao", source = "descricao")
    @Mapping(target = "atividadesCodigos", source = "atividades", qualifiedByName = "mapAtividadesCodigos")
    CompetenciaMapaDto toDto(Competencia competencia);

    @Named("mapAtividadesCodigos")
    default List<Long> mapAtividadesCodigos(java.util.Set<sgc.atividade.internal.model.Atividade> atividades) {
        if (atividades == null) return null;

        return atividades.stream()
                .filter(Objects::nonNull)
                .map(sgc.atividade.internal.model.Atividade::getCodigo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}

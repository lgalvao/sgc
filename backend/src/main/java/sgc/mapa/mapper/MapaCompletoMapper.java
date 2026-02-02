package sgc.mapa.mapper;

import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MapaCompletoMapper {
    default MapaCompletoDto toDto(Mapa mapa, Long codSubprocesso, List<Competencia> competencias) {
        return MapaCompletoDto.builder()
                .codigo(mapa.getCodigo())
                .subprocessoCodigo(codSubprocesso)
                .observacoes(mapa.getObservacoesDisponibilizacao())
                .competencias(competencias.stream().map(this::toDto).toList())
                .build();
    }

    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "descricao", source = "descricao")
    @Mapping(target = "atividadesCodigos", source = "atividades", qualifiedByName = "mapAtividadesCodigos")
    CompetenciaMapaDto toDto(@Nullable Competencia competencia);

    @Named("mapAtividadesCodigos")
    default List<Long> mapAtividadesCodigos(Set<Atividade> atividades) {

        return atividades.stream()
                .map(Atividade::getCodigo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}

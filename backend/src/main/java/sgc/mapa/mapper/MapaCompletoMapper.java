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
                .codigo(mapa.getCodigo())
                .subprocessoCodigo(codSubprocesso)
                .observacoes(mapa.getObservacoesDisponibilizacao())
                .competencias(competencias.stream().map(this::toDto).toList())
                .build();
    }

    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "descricao", source = "descricao")
    @Mapping(target = "atividadesCodigos", source = "atividades", qualifiedByName = "mapAtividadesCodigos")
    CompetenciaMapaDto toDto(Competencia competencia);

    @Named("mapAtividadesCodigos")
    default List<Long> mapAtividadesCodigos(java.util.Set<sgc.mapa.model.Atividade> atividades) {

        return atividades.stream()
                .map(sgc.mapa.model.Atividade::getCodigo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}

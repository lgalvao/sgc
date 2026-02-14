package sgc.mapa.mapper;

import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import sgc.comum.config.CentralMapperConfig;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;

import java.util.List;
import java.util.Set;
import org.mapstruct.ReportingPolicy;

@SuppressWarnings("NullableProblems")
@Mapper(config = CentralMapperConfig.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MapaCompletoMapper {
    default MapaCompletoDto toDto(Mapa mapa, @Nullable Long codSubprocesso, List<Competencia> competencias) {
        return MapaCompletoDto.builder()
                .codigo(mapa.getCodigo())
                .subprocessoCodigo(codSubprocesso)
                .observacoes(mapa.getObservacoesDisponibilizacao())
                .competencias(competencias.stream()
                        .map(this::toDto)
                        .toList())
                .build();
    }

    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "descricao", source = "descricao")
    @Mapping(target = "atividadesCodigos", source = "atividades", qualifiedByName = "mapAtividadesCodigos")
    CompetenciaMapaDto toDto(Competencia competencia);

    @Named("mapAtividadesCodigos")
    default List<Long> mapAtividadesCodigos(Set<Atividade> atividades) {
        return atividades.stream()
                .map(Atividade::getCodigo)
                .toList();
    }
}
package sgc.mapa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.atividade.model.Atividade;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MapaCompletoMapper {

    @Mapping(target = "codigo", source = "mapa.codigo")
    @Mapping(target = "subprocessoCodigo", source = "subprocessoCodigo")
    @Mapping(target = "observacoes", source = "mapa.observacoesDisponibilizacao")
    @Mapping(target = "competencias", source = "competencias")
    MapaCompletoDto toDto(Mapa mapa, Long subprocessoCodigo, List<Competencia> competencias);

    @Mapping(target = "codigo", source = "competencia.codigo")
    @Mapping(target = "descricao", source = "competencia.descricao")
    @Mapping(target = "atividadesCodigos", expression = "java(mapAtividades(competencia.getAtividades()))")
    CompetenciaMapaDto toCompetenciaDto(Competencia competencia);

    default List<Long> mapAtividades(Set<Atividade> atividades) {
        if (atividades == null) {
            return Collections.emptyList();
        }
        return atividades.stream()
                .map(Atividade::getCodigo)
                .collect(Collectors.toList());
    }
}

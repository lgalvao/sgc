package sgc.subprocesso.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.analise.model.Analise;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.dto.ConhecimentoAjusteDto;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.model.Subprocesso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MapaAjusteMapper {
    @Mapping(target = "codMapa", source = "sp.mapa.codigo")
    @Mapping(target = "unidadeNome", source = "sp.unidade.nome")
    @Mapping(target = "competencias", expression = "java(mapCompetencias(competencias, atividades, conhecimentos, associacoes))")
    @Mapping(target = "justificativaDevolucao", source = "analise.observacoes")
    MapaAjusteDto toDto(Subprocesso sp, @org.jspecify.annotations.Nullable Analise analise, List<Competencia> competencias, List<Atividade> atividades, List<Conhecimento> conhecimentos, Map<Long, java.util.Set<Long>> associacoes);

    default List<CompetenciaAjusteDto> mapCompetencias(List<Competencia> competencias, List<Atividade> atividades, List<Conhecimento> conhecimentos, Map<Long, java.util.Set<Long>> associacoes) {
        Map<Long, List<Conhecimento>> conhecimentosPorAtividade = conhecimentos.stream()
                .collect(Collectors.groupingBy(Conhecimento::getCodigoAtividade));

        List<CompetenciaAjusteDto> competenciaDtos = new ArrayList<>();

        for (Competencia comp : competencias) {
            List<AtividadeAjusteDto> atividadeDtos = new ArrayList<>();
            java.util.Set<Long> atividadesAssociadas = associacoes.getOrDefault(comp.getCodigo(), Collections.emptySet());

            for (Atividade ativ : atividades) {
                List<Conhecimento> conhecimentosDaAtividade =
                        conhecimentosPorAtividade.getOrDefault(ativ.getCodigo(), Collections.emptyList());

                boolean isLinked = atividadesAssociadas.contains(ativ.getCodigo());
                List<ConhecimentoAjusteDto> conhecimentoDtos =
                        conhecimentosDaAtividade.stream()
                                .map(con -> ConhecimentoAjusteDto.builder()
                                        .conhecimentoCodigo(con.getCodigo())
                                        .nome(con.getDescricao())
                                        .incluido(isLinked)
                                        .build())
                                .toList();

                atividadeDtos.add(AtividadeAjusteDto.builder()
                        .codAtividade(ativ.getCodigo())
                        .nome(ativ.getDescricao())
                        .conhecimentos(conhecimentoDtos)
                        .build());
            }

            competenciaDtos.add(CompetenciaAjusteDto.builder()
                    .codCompetencia(comp.getCodigo())
                    .nome(comp.getDescricao())
                    .atividades(atividadeDtos)
                    .build());
        }
        return competenciaDtos;
    }
}

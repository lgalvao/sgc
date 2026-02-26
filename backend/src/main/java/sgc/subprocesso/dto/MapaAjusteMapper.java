package sgc.subprocesso.dto;

import org.mapstruct.*;
import sgc.comum.config.*;
import sgc.mapa.model.*;
import sgc.subprocesso.model.*;

import java.util.*;
import java.util.stream.*;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class)
public interface MapaAjusteMapper {
    @Mapping(target = "codMapa", source = "sp.mapa.codigo")
    @Mapping(target = "unidadeNome", source = "sp.unidade.nome")
    @Mapping(target = "competencias", expression = "java(mapCompetencias(competencias, atividades, conhecimentos, associacoes))")
    @Mapping(target = "justificativaDevolucao", source = "analise.observacoes")
    MapaAjusteDto toDto(Subprocesso sp, Analise analise, List<Competencia> competencias, List<Atividade> atividades, List<Conhecimento> conhecimentos, @Context Map<Long, Set<Long>> associacoes);

    default List<CompetenciaAjusteDto> mapCompetencias(List<Competencia> competencias, List<Atividade> atividades, List<Conhecimento> conhecimentos, Map<Long, Set<Long>> associacoes) {
        Map<Long, List<Conhecimento>> conhecimentosPorAtividade = conhecimentos.stream()
                .collect(Collectors.groupingBy(Conhecimento::getCodigoAtividade));

        List<CompetenciaAjusteDto> competenciaDtos = new ArrayList<>();

        for (Competencia comp : competencias) {
            List<AtividadeAjusteDto> atividadeDtos = new ArrayList<>();
            Set<Long> atividadesAssociadas = associacoes.getOrDefault(comp.getCodigo(), Collections.emptySet());

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

package sgc.subprocesso.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.analise.internal.model.Analise;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.Conhecimento;
import sgc.mapa.model.Competencia;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.dto.ConhecimentoAjusteDto;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.model.Subprocesso;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MapaAjusteMapper {

    @Mapping(target = "codMapa", source = "sp.mapa.codigo")
    @Mapping(target = "unidadeNome", source = "sp.unidade.nome")
    @Mapping(target = "competencias", expression = "java(mapCompetencias(competencias, atividades, conhecimentos))")
    @Mapping(target = "justificativaDevolucao", source = "analise.observacoes")
    MapaAjusteDto toDto(Subprocesso sp, Analise analise, List<Competencia> competencias, List<Atividade> atividades, List<Conhecimento> conhecimentos);

    default List<CompetenciaAjusteDto> mapCompetencias(List<Competencia> competencias, List<Atividade> atividades, List<Conhecimento> conhecimentos) {
        List<CompetenciaAjusteDto> competenciaDtos = new ArrayList<>();

        for (Competencia comp : competencias) {
            List<AtividadeAjusteDto> atividadeDtos = new ArrayList<>();
            for (Atividade ativ : atividades) {
                List<Conhecimento> conhecimentosDaAtividade =
                        conhecimentos.stream()
                                .filter(c -> c.getAtividade().getCodigo().equals(ativ.getCodigo()))
                                .collect(Collectors.toList());
                boolean isLinked = comp.getAtividades().contains(ativ);
                List<ConhecimentoAjusteDto> conhecimentoDtos =
                        conhecimentosDaAtividade.stream()
                                .map(
                                        con ->
                                                ConhecimentoAjusteDto.builder()
                                                        .conhecimentoCodigo(con.getCodigo())
                                                        .nome(con.getDescricao())
                                                        .incluido(isLinked)
                                                        .build())
                                .collect(Collectors.toList());

                atividadeDtos.add(
                        AtividadeAjusteDto.builder()
                                .codAtividade(ativ.getCodigo())
                                .nome(ativ.getDescricao())
                                .conhecimentos(conhecimentoDtos)
                                .build());
            }

            competenciaDtos.add(
                    CompetenciaAjusteDto.builder()
                            .codCompetencia(comp.getCodigo())
                            .nome(comp.getDescricao())
                            .atividades(atividadeDtos)
                            .build());
        }
        return competenciaDtos;
    }
}

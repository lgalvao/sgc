package sgc.mapa;

import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.util.*;
import java.util.stream.*;

@Component
public class MapaDtoMapper {

    public AtividadeDto paraAtividadeDto(Atividade atividade) {
        return AtividadeDto.builder()
                .codigo(atividade.getCodigo())
                .descricao(atividade.getDescricao())
                .conhecimentos(paraConhecimentosResumo(atividade.getConhecimentos()))
                .build();
    }

    public ConhecimentoResumoDto paraConhecimentoResumoDto(Conhecimento conhecimento) {
        return new ConhecimentoResumoDto(
                conhecimento.getCodigo(),
                conhecimento.getDescricao());
    }

    public List<ConhecimentoResumoDto> paraConhecimentosResumo(Collection<Conhecimento> conhecimentos) {
        if (conhecimentos.isEmpty()) {
            return List.of();
        }
        return conhecimentos.stream()
                .map(this::paraConhecimentoResumoDto)
                .toList();
    }

    public AtividadeMapaDto paraAtividadeMapaDto(Atividade atividade) {
        return new AtividadeMapaDto(
                atividade.getCodigo(),
                atividade.getDescricao(),
                paraConhecimentosResumo(atividade.getConhecimentos()));
    }

    public CompetenciaMapaDto paraCompetenciaMapaDto(Competencia competencia) {
        Set<Atividade> atividades = competencia.getAtividades();
        return new CompetenciaMapaDto(
                competencia.getCodigo(),
                competencia.getDescricao(),
                atividades.stream()
                        .map(this::paraAtividadeMapaDto)
                        .toList());
    }

    public MapaResumoDto paraMapaResumoDto(Mapa mapa) {
        return MapaResumoDto.builder()
                .codigo(mapa.getCodigo())
                .subprocessoCodigo(mapa.getSubprocesso().getCodigo())
                .dataHoraDisponibilizado(mapa.getDataHoraDisponibilizado())
                .observacoesDisponibilizacao(mapa.getObservacoesDisponibilizacao())
                .sugestoes(mapa.getSugestoes())
                .dataHoraHomologado(mapa.getDataHoraHomologado())
                .build();
    }

    public MapaCompletoDto paraMapaCompletoDto(Mapa mapa) {
        Set<Competencia> competencias = mapa.getCompetencias();
        Set<Atividade> atividades = mapa.getAtividades();
        return new MapaCompletoDto(
                mapa.getCodigo(),
                mapa.getSubprocesso().getCodigo(),
                mapa.getObservacoesDisponibilizacao(),
                competencias.stream()
                        .map(this::paraCompetenciaMapaDto)
                        .toList(),
                atividades.stream()
                        .map(this::paraAtividadeMapaDto)
                        .toList(),
                null);
    }

    public MapaAjusteDto paraMapaAjusteDto(
            Subprocesso subprocesso,
            @Nullable Analise analise,
            List<Competencia> competencias,
            List<Atividade> atividades,
            List<Conhecimento> conhecimentos) {
        Map<Long, List<Conhecimento>> conhecimentosPorAtividade = conhecimentos.stream()
                .collect(Collectors.groupingBy(conhecimento -> conhecimento.getAtividade().getCodigo()));

        List<CompetenciaAjusteDto> competenciaDtos = competencias.stream()
                .map(competencia -> paraCompetenciaAjusteDto(competencia, atividades, conhecimentosPorAtividade))
                .toList();

        return MapaAjusteDto.builder()
                .codMapa(subprocesso.getMapa().getCodigo())
                .unidadeNome(subprocesso.getUnidade().getNome())
                .competencias(competenciaDtos)
                .justificativaDevolucao(analise != null ? analise.getObservacoes() : null)
                .build();
    }

    private CompetenciaAjusteDto paraCompetenciaAjusteDto(
            Competencia competencia,
            List<Atividade> atividades,
            Map<Long, List<Conhecimento>> conhecimentosPorAtividade) {
        return CompetenciaAjusteDto.builder()
                .codCompetencia(competencia.getCodigo())
                .nome(competencia.getDescricao())
                .atividades(atividades.stream()
                        .map(atividade -> paraAtividadeAjusteDto(competencia, atividade, conhecimentosPorAtividade))
                        .toList())
                .build();
    }

    private AtividadeAjusteDto paraAtividadeAjusteDto(
            Competencia competencia,
            Atividade atividade,
            Map<Long, List<Conhecimento>> conhecimentosPorAtividade) {
        boolean vinculada = competencia.getAtividades().contains(atividade);
        List<ConhecimentoAjusteDto> conhecimentos = conhecimentosPorAtividade
                .getOrDefault(atividade.getCodigo(), List.of())
                .stream()
                .map(conhecimento -> paraConhecimentoAjusteDto(conhecimento, vinculada))
                .toList();

        return AtividadeAjusteDto.builder()
                .codAtividade(atividade.getCodigo())
                .nome(atividade.getDescricao())
                .conhecimentos(conhecimentos)
                .build();
    }

    private ConhecimentoAjusteDto paraConhecimentoAjusteDto(Conhecimento conhecimento, boolean incluido) {
        return ConhecimentoAjusteDto.builder()
                .conhecimentoCodigo(conhecimento.getCodigo())
                .nome(conhecimento.getDescricao())
                .incluido(incluido)
                .build();
    }
}

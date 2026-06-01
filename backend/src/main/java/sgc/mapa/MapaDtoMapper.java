package sgc.mapa;

import org.springframework.stereotype.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;

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
        if (conhecimentos == null || conhecimentos.isEmpty()) {
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
                (atividades == null ? Stream.<Atividade>empty() : atividades.stream())
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
                (competencias == null ? Stream.<Competencia>empty() : competencias.stream())
                        .map(this::paraCompetenciaMapaDto)
                        .toList(),
                (atividades == null ? Stream.<Atividade>empty() : atividades.stream())
                        .map(this::paraAtividadeMapaDto)
                        .toList(),
                null);
    }
}

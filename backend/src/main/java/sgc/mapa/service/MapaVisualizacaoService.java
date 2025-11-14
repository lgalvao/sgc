package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.visualizacao.AtividadeDto;
import sgc.mapa.dto.visualizacao.CompetenciaDto;
import sgc.mapa.dto.visualizacao.ConhecimentoDto;
import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class MapaVisualizacaoService {
    private final SubprocessoRepo subprocessoRepo;
    private final CompetenciaRepo competenciaRepo;
    private final ConhecimentoRepo conhecimentoRepo;

    public MapaVisualizacaoDto obterMapaParaVisualizacao(Long codSubprocesso) {
        Subprocesso subprocesso = subprocessoRepo.findById(codSubprocesso)
            .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));

        if (subprocesso.getMapa() == null) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não possui mapa associado: ", codSubprocesso);
        }

        Unidade unidade = subprocesso.getUnidade();
        var unidadeDto = new MapaVisualizacaoDto.UnidadeDto(unidade.getCodigo(), unidade.getSigla(), unidade.getNome());

        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(subprocesso.getMapa().getCodigo());

        List<CompetenciaDto> competenciasDto = competencias.stream().map(competencia -> {
            List<Atividade> atividades = competencia.getAtividades().stream().toList();

            List<AtividadeDto> atividadesDto = atividades.stream().map(atividade -> {
                List<Conhecimento> conhecimentos = conhecimentoRepo.findByAtividadeCodigo(atividade.getCodigo());
                List<ConhecimentoDto> conhecimentosDto = conhecimentos.stream()
                    .map(c -> new ConhecimentoDto(c.getCodigo(), c.getDescricao()))
                    .toList();
                return new AtividadeDto(atividade.getCodigo(), atividade.getDescricao(), conhecimentosDto);
            }).toList();

            return new CompetenciaDto(competencia.getCodigo(), competencia.getDescricao(), atividadesDto);
        }).toList();

        return new MapaVisualizacaoDto(unidadeDto, competenciasDto);
    }
}

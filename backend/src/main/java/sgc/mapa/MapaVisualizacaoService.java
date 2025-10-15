package sgc.mapa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.modelo.Atividade;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.mapa.dto.visualizacao.AtividadeDto;
import sgc.mapa.dto.visualizacao.CompetenciaDto;
import sgc.mapa.dto.visualizacao.ConhecimentoDto;
import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class MapaVisualizacaoService {

    private final SubprocessoRepo repositorioSubprocesso;
    private final CompetenciaRepo repositorioCompetencia;
    private final CompetenciaAtividadeRepo repositorioCompetenciaAtividade;

    /**
     * Obtém um mapa completo formatado para visualização, conforme CDU-18.
     *
     * @param subprocessoId O ID do subprocesso.
     * @return Um DTO com os dados do mapa prontos para exibição.
     */
    public MapaVisualizacaoDto obterMapaParaVisualizacao(Long subprocessoId) {
        log.debug("Obtendo mapa para visualização do subprocesso: id={}", subprocessoId);

        Subprocesso subprocesso = repositorioSubprocesso.findById(subprocessoId)
            .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: " + subprocessoId));

        if (subprocesso.getMapa() == null) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não possui mapa associado.");
        }

        Unidade unidade = subprocesso.getUnidade();
        var unidadeDto = new MapaVisualizacaoDto.UnidadeDto(unidade.getCodigo(), unidade.getSigla(), unidade.getNome());

        List<Competencia> competencias = repositorioCompetencia.findByMapaCodigo(subprocesso.getMapa().getCodigo());

        List<CompetenciaDto> competenciasDto = competencias.stream().map(competencia -> {
            List<Atividade> atividades = repositorioCompetenciaAtividade.findByCompetenciaCodigo(competencia.getCodigo())
                .stream()
                .map(CompetenciaAtividade::getAtividade)
                .toList();

            List<AtividadeDto> atividadesDto = atividades.stream().map(atividade -> {
                List<Conhecimento> conhecimentos = atividade.getConhecimentos();
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
package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.modelo.Atividade;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.erros.ErroDominioNaoEncontrado;
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
    private final sgc.conhecimento.modelo.ConhecimentoRepo repositorioConhecimento;

    /**
     * Monta uma estrutura de dados aninhada de um mapa para fins de visualização.
     * <p>
     * Este método, correspondente ao CDU-18, busca um subprocesso e seu mapa
     * associado, e então constrói um DTO que representa a hierarquia completa:
     * Mapa -> Competências -> Atividades -> Conhecimentos.
     *
     * @param subprocessoId O código do subprocesso a partir do qual o mapa será obtido.
     * @return Um {@link MapaVisualizacaoDto} com a estrutura completa do mapa.
     * @throws ErroDominioNaoEncontrado se o subprocesso ou o mapa associado não forem encontrados.
     */
    public MapaVisualizacaoDto obterMapaParaVisualizacao(Long subprocessoId) {
        log.debug("Obtendo mapa para visualização do subprocesso: codigo={}", subprocessoId);

        Subprocesso subprocesso = repositorioSubprocesso.findById(subprocessoId)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso", subprocessoId));

        if (subprocesso.getMapa() == null) {
            throw new ErroDominioNaoEncontrado("Subprocesso não possui mapa associado.");
        }

        Unidade unidade = subprocesso.getUnidade();
        var unidadeDto = new MapaVisualizacaoDto.UnidadeDto(unidade.getCodigo(), unidade.getSigla(), unidade.getNome());

        List<Competencia> competencias = repositorioCompetencia.findByMapaCodigo(subprocesso.getMapa().getCodigo());

        List<CompetenciaDto> competenciasDto = competencias.stream().map(competencia -> {
            List<Atividade> atividades = repositorioCompetenciaAtividade.findByCompetencia_Codigo(competencia.getCodigo())
                .stream()
                .map(CompetenciaAtividade::getAtividade)
                .toList();

            List<AtividadeDto> atividadesDto = atividades.stream().map(atividade -> {
                List<Conhecimento> conhecimentos = repositorioConhecimento.findByAtividadeCodigo(atividade.getCodigo());
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
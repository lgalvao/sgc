package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaAtividade;
import sgc.mapa.model.CompetenciaAtividadeRepo;
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
    private final CompetenciaAtividadeRepo competenciaAtividadeRepo;

    /**
     * Monta uma estrutura de dados aninhada de um mapa para fins de visualização.
     * <p>
     * Busca um subprocesso e seu mapa associado, e constrói um DTO que representa a hierarquia completa:
     * Mapa -> Competências -> Atividades -> Conhecimentos.
     *
     * @param codSubprocesso O código do subprocesso a partir do qual o mapa será obtido.
     * @return Um {@link MapaVisualizacaoDto} com a estrutura completa do mapa.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso ou o mapa associado não forem encontrados.
     */
    public MapaVisualizacaoDto obterMapaParaVisualizacao(Long codSubprocesso) {
        log.debug("Obtendo mapa para visualização do subprocesso: codigo={}", codSubprocesso);

        Subprocesso subprocesso = subprocessoRepo.findById(codSubprocesso)
            .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));

        // TODO nao é precipitadao lançar essa exceção aqui? Nem deveria acontecer se as camadas de cima fizerem sua parte.
        if (subprocesso.getMapa() == null) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não possui mapa associado.");
        }

        Unidade unidade = subprocesso.getUnidade();
        var unidadeDto = new MapaVisualizacaoDto.UnidadeDto(unidade.getCodigo(), unidade.getSigla(), unidade.getNome());

        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(subprocesso.getMapa().getCodigo());

        List<CompetenciaDto> competenciasDto = competencias.stream().map(competencia -> {
            List<Atividade> atividades = competenciaAtividadeRepo.findByCompetenciaCodigo(competencia.getCodigo())
                .stream()
                .map(CompetenciaAtividade::getAtividade)
                .toList();

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
package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseFacade;
import sgc.analise.model.Analise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.service.MapaManutencaoService;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service responsável por operações relacionadas a ajustes de mapa em subprocessos.
 * 
 * <p>Extrai lógica de ajuste de mapas que estava em métodos privados de {@link SubprocessoFacade}.
 * Responsabilidades:
 * <ul>
 *   <li>Salvar ajustes de mapa (competências e atividades)</li>
 *   <li>Validar situações permitidas para ajuste</li>
 *   <li>Obter mapa preparado para ajuste</li>
 *   <li>Atualizar descrições de atividades em lote</li>
 *   <li>Atualizar competências e suas associações</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
class SubprocessoAjusteMapaService {

    private final SubprocessoRepo subprocessoRepo;
    private final SubprocessoCrudService crudService;
    private final MapaManutencaoService mapaManutencaoService;
    private final AnaliseFacade analiseFacade;
    private final MapaAjusteMapper mapaAjusteMapper;

    /**
     * Salva ajustes feitos no mapa de um subprocesso.
     * 
     * @param codSubprocesso código do subprocesso
     * @param competencias lista de competências ajustadas
     * @throws ErroEntidadeNaoEncontrada se subprocesso não existe
     * @throws sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida se situação não permite ajuste
     */
    @Transactional
    public void salvarAjustesMapa(Long codSubprocesso, List<CompetenciaAjusteDto> competencias) {
        Subprocesso sp = subprocessoRepo.findById(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                        "Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        validarSituacaoParaAjuste(sp);
        atualizarDescricoesAtividades(competencias);
        atualizarCompetenciasEAssociacoes(competencias);

        sp.setSituacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        subprocessoRepo.save(sp);
    }

    /**
     * Obtém mapa preparado para ajuste.
     * 
     * @param codSubprocesso código do subprocesso
     * @return DTO com dados do mapa para ajuste
     */
    @Transactional(readOnly = true)
    public MapaAjusteDto obterMapaParaAjuste(Long codSubprocesso) {
        Subprocesso sp = crudService.buscarSubprocessoComMapa(codSubprocesso);
        Long codMapa = sp.getMapa().getCodigo();
        
        Analise analise = analiseFacade.listarPorSubprocesso(codSubprocesso, TipoAnalise.VALIDACAO)
                .stream()
                .findFirst()
                .orElse(null);
        
        List<Competencia> competencias = mapaManutencaoService.buscarCompetenciasPorCodMapaSemRelacionamentos(codMapa);
        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoSemRelacionamentos(codMapa);
        List<Conhecimento> conhecimentos = mapaManutencaoService.listarConhecimentosPorMapa(codMapa);
        Map<Long, Set<Long>> associacoes = mapaManutencaoService.buscarIdsAssociacoesCompetenciaAtividade(codMapa);
        
        var dto = mapaAjusteMapper.toDto(sp, analise, competencias, atividades, conhecimentos, associacoes);
        if (dto == null) {
            throw new sgc.comum.erros.ErroEstadoImpossivel("Falha ao gerar dados de ajuste do mapa.");
        }
        return dto;
    }

    /**
     * Valida se a situação do subprocesso permite ajuste de mapa.
     * 
     * @param sp subprocesso a validar
     * @throws sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida se situação inválida
     */
    private void validarSituacaoParaAjuste(Subprocesso sp) {
        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA
                && sp.getSituacao() != SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO) {
            throw new ErroMapaEmSituacaoInvalida(
                    "Ajustes no mapa só podem ser feitos em estados específicos. "
                            + "Situação atual: %s".formatted(sp.getSituacao()));
        }
    }

    /**
     * Atualiza descrições de atividades em lote.
     * 
     * @param competencias competências com atividades a atualizar
     */
    private void atualizarDescricoesAtividades(List<CompetenciaAjusteDto> competencias) {
        Map<Long, String> atividadeDescricoes = new HashMap<>();
        for (CompetenciaAjusteDto compDto : competencias) {
            for (AtividadeAjusteDto ativDto : compDto.getAtividades()) {
                atividadeDescricoes.put(ativDto.codAtividade(), ativDto.nome());
            }
        }
        if (!atividadeDescricoes.isEmpty()) {
            mapaManutencaoService.atualizarDescricoesAtividadeEmLote(atividadeDescricoes);
        }
    }

    /**
     * Atualiza competências e suas associações com atividades.
     * 
     * @param competencias competências com dados atualizados
     */
    private void atualizarCompetenciasEAssociacoes(List<CompetenciaAjusteDto> competencias) {
        // Carregar todas as competências envolvidas
        List<Long> competenciaIds = competencias.stream()
                .map(CompetenciaAjusteDto::getCodCompetencia)
                .toList();

        Map<Long, Competencia> mapaCompetencias = mapaManutencaoService.buscarCompetenciasPorCodigos(competenciaIds)
                .stream()
                .collect(Collectors.toMap(Competencia::getCodigo, Function.identity()));

        List<Long> todasAtividadesIds = competencias.stream()
                .flatMap(c -> c.getAtividades().stream())
                .map(AtividadeAjusteDto::codAtividade)
                .distinct()
                .toList();

        Map<Long, Atividade> mapaAtividades = mapaManutencaoService.buscarAtividadesPorCodigos(todasAtividadesIds)
                .stream()
                .collect(Collectors.toMap(Atividade::getCodigo, Function.identity()));

        List<Competencia> competenciasParaSalvar = new ArrayList<>();
        for (CompetenciaAjusteDto compDto : competencias) {
            Competencia competencia = mapaCompetencias.get(compDto.getCodCompetencia());
            if (competencia != null) {
                competencia.setDescricao(compDto.getNome());

                Set<Atividade> atividadesSet = new HashSet<>();
                for (AtividadeAjusteDto ativDto : compDto.getAtividades()) {
                    Atividade ativ = mapaAtividades.get(ativDto.codAtividade());
                    atividadesSet.add(ativ);
                }
                competencia.setAtividades(atividadesSet);
                competenciasParaSalvar.add(competencia);
            }
        }
        mapaManutencaoService.salvarTodasCompetencias(competenciasParaSalvar);
    }
}

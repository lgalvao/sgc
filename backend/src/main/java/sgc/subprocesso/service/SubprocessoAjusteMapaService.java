package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.model.ComumRepo;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.service.MapaManutencaoService;
import sgc.subprocesso.AnaliseFacade;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.dto.MapaAjusteMapper;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Operações relacionadas a ajustes de mapa em subprocessos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
class SubprocessoAjusteMapaService {

    private final SubprocessoRepo subprocessoRepo;
    private final ComumRepo repo;
    private final MapaManutencaoService mapaManutencaoService;
    private final AnaliseFacade analiseFacade;
    private final MapaAjusteMapper mapaAjusteMapper;

    @Transactional
    public void salvarAjustesMapa(Long codSubprocesso, List<CompetenciaAjusteDto> competencias) {
        log.info("Salvando ajustes no mapa do subprocesso {} ({} competências)", codSubprocesso, competencias.size());
        Subprocesso sp = repo.buscar(Subprocesso.class, codSubprocesso);

        validarSituacaoParaAjuste(sp);
        atualizarDescricoesAtividades(competencias);
        atualizarCompetenciasEAssociacoes(competencias);

        sp.setSituacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        subprocessoRepo.save(sp);
    }

    @Transactional(readOnly = true)
    public MapaAjusteDto obterMapaParaAjuste(Long codSubprocesso) {
        log.info("Recuperando mapa para ajustes do subprocesso {}", codSubprocesso);
        Subprocesso sp = subprocessoRepo.findByIdWithMapaAndAtividades(codSubprocesso).orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));
        Long codMapa = sp.getMapa().getCodigo();
        
        Analise analise = analiseFacade.listarPorSubprocesso(codSubprocesso, TipoAnalise.VALIDACAO)
                .stream()
                .findFirst()
                .orElse(null);
        
        List<Competencia> competencias = mapaManutencaoService.buscarCompetenciasPorCodMapaSemRelacionamentos(codMapa);
        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoSemRelacionamentos(codMapa);
        List<Conhecimento> conhecimentos = mapaManutencaoService.listarConhecimentosPorMapa(codMapa);
        Map<Long, Set<Long>> associacoes = mapaManutencaoService.buscarIdsAssociacoesCompetenciaAtividade(codMapa);
        
        return mapaAjusteMapper.toDto(sp, analise, competencias, atividades, conhecimentos, associacoes);
    }

    private void validarSituacaoParaAjuste(Subprocesso sp) {
        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA
                && sp.getSituacao() != SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO) {
            throw new ErroMapaEmSituacaoInvalida(
                    "Ajustes no mapa só podem ser feitos em estados específicos. "
                            + "Situação atual: %s".formatted(sp.getSituacao()));
        }
    }

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

    private void atualizarCompetenciasEAssociacoes(List<CompetenciaAjusteDto> competencias) {
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

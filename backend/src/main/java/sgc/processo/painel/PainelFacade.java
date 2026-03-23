package sgc.processo.painel;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.*;
import sgc.alerta.model.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.processo.service.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PainelFacade {
    private final AlertaFacade alertaFacade;
    private final UnidadeHierarquiaService hierarquiaService;
    private final UnidadeService unidadeService;
    private final ProcessoService processoService;

    /**
     * Lista processos com base no perfil e na unidade do usuário.
     *
     * <p>Regras de visibilidade:
     * - ADMIN: vê todos os processos
     * - GESTOR: vê processos da unidade e de todas as subordinadas (recursivamente)
     * - CHEFE e SERVIDOR: veem processos APENAS da própria unidade
     * - Processos no estado 'CRIADO' são omitidos para perfis não-ADMIN
     */
    public Page<ProcessoResumoDto> listarProcessos(Perfil perfil, Long codigoUnidade, Pageable pageable) {
        Pageable sortedPageable = garantirOrdenacaoPadrao(pageable);
        Page<Processo> processos;

        Map<Long, List<Long>> mapaPaiFilhos = hierarquiaService.buscarMapaHierarquia();
        if (perfil == Perfil.ADMIN) {
            processos = processoService.listarTodos(sortedPageable);
        } else {
            List<Long> codigosUnidades = new ArrayList<>();
            if (perfil == Perfil.GESTOR) {
                codigosUnidades.addAll(hierarquiaService.buscarDescendentes(codigoUnidade, mapaPaiFilhos));
            }
            codigosUnidades.add(codigoUnidade);
            processos = processoService.listarIniciadosPorParticipantes(codigosUnidades, sortedPageable);
        }
        return processos.map(processo -> paraProcessoResumoDto(processo, perfil, codigoUnidade, mapaPaiFilhos));
    }

    private Pageable garantirOrdenacaoPadrao(Pageable pageable) {
        if (pageable.isUnpaged()) {
            return pageable;
        }
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "dataCriacao").and(Sort.by(Sort.Direction.DESC, "codigo")));
        }
        return pageable;
    }

    @Transactional
    public Page<Alerta> listarAlertas(String usuarioTitulo, Long codigoUnidade, String perfil, Pageable pageable) {
        // Regra CDU-02 (3.3): Os alertas devem estar ordenados de forma decrescente por data/hora, 
        // nao sendo permitida a reordenação.
        Pageable sortedPageable = pageable.isPaged() 
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "dataHora"))
                : pageable;

        // Alertas são filtrados pela unidade do usuário ou alertas pessoais (sem subordinadas - Regra CDU-02)
        Page<Alerta> alertasPage = alertaFacade.listarPorUnidade(usuarioTitulo, codigoUnidade, perfil, sortedPageable);
        List<Long> alertasNaoLidosVisualizados = new ArrayList<>();
        alertasPage.forEach(alerta -> {
            LocalDateTime dataHoraLeitura = alertaFacade.obterDataHoraLeitura(alerta.getCodigo(), usuarioTitulo).orElse(null);
            if (dataHoraLeitura == null) {
                alertasNaoLidosVisualizados.add(alerta.getCodigo());
            }
            alerta.setDataHoraLeitura(dataHoraLeitura);
        });

        if (!alertasNaoLidosVisualizados.isEmpty()) {
            alertaFacade.marcarComoLidos(usuarioTitulo, alertasNaoLidosVisualizados);
        }

        return alertasPage;
    }

    private ProcessoResumoDto paraProcessoResumoDto(Processo processo,
                                                    Perfil perfil,
                                                    Long codigoUnidade,
                                                    Map<Long, List<Long>> mapaPaiFilhos) {

        var participantes = processo.getParticipantes();
        var participante = participantes.getFirst();

        Long codUnidMapeado = participante.getUnidadeCodigo();
        String nomeUnidMapeado = participante.getNome();

        String linkDestino = calcularLinkDestinoProcesso(processo, perfil, codigoUnidade);
        String unidadesParticipantes = formatarUnidadesParticipantes(participantes, mapaPaiFilhos);

        return ProcessoResumoDto.builder()
                .codigo(processo.getCodigo())
                .descricao(processo.getDescricao())
                .situacao(processo.getSituacao())
                .tipo(processo.getTipo().name())
                .dataLimite(processo.getDataLimite())
                .dataCriacao(processo.getDataCriacao())
                .unidadeCodigo(codUnidMapeado)
                .unidadeNome(nomeUnidMapeado)
                .unidadesParticipantes(unidadesParticipantes)
                .linkDestino(linkDestino)
                .build();
    }

    private String formatarUnidadesParticipantes(List<UnidadeProcesso> participantes,
                                                 Map<Long, List<Long>> mapaPaiFilhos) {

        Map<Long, Long> mapaFilhoPai = new HashMap<>();
        mapaPaiFilhos.forEach((key, value) -> value.forEach(filho -> mapaFilhoPai.put(filho, key)));

        Set<Long> participantesIds = participantes.stream()
                .map(UnidadeProcesso::getUnidadeCodigo)
                .collect(Collectors.toSet());

        Set<Long> displayIds = new HashSet<>();
        Map<Long, Boolean> coveredCache = new HashMap<>();
        for (Long codUnidade : participantesIds) {
            Long candidate = codUnidade;
            Long parent = mapaFilhoPai.get(candidate);

            while (parent != null && mapaFilhoPai.get(parent) != null) {
                // Agrupa no pai se ele estiver totalmente coberto pelas unidades participantes.
                // Interrompe o agrupamento no nível imediatamente abaixo da unidade raiz.
                if (isCovered(parent, participantesIds, mapaPaiFilhos, coveredCache)) {
                    candidate = parent;
                    parent = mapaFilhoPai.get(candidate);
                } else {
                    break;
                }
            }
            displayIds.add(candidate);
        }

        Map<Long, String> existingSiglas = participantes.stream().collect(toMap(
                UnidadeProcesso::getUnidadeCodigo,
                UnidadeProcesso::getSigla,
                (s1, s2) -> s1)
        );

        List<String> siglas = new ArrayList<>();
        List<Long> missingIds = new ArrayList<>();
        displayIds.forEach(codUnidade -> {
            if (existingSiglas.containsKey(codUnidade)) siglas.add(existingSiglas.get(codUnidade));
            else missingIds.add(codUnidade);
        });

        if (!missingIds.isEmpty()) {
            List<Unidade> missingUnidades = unidadeService.porCodigos(missingIds);
            missingUnidades.forEach(u -> siglas.add(u.getSigla()));
        }

        return siglas.stream().sorted().collect(Collectors.joining(", "));
    }

    private boolean isCovered(Long codUnidade,
                              Set<Long> participantesIds,
                              Map<Long, List<Long>> mapaPaiFilhos,
                              Map<Long, Boolean> cache) {

        if (participantesIds.contains(codUnidade)) return true;

        if (cache.containsKey(codUnidade)) return cache.get(codUnidade);

        List<Long> children = mapaPaiFilhos.get(codUnidade);
        if (children == null || children.isEmpty()) {
            cache.put(codUnidade, false);
            return false;
        }

        boolean allCovered = true;
        for (Long codFilho : children) {
            if (!isCovered(codFilho, participantesIds, mapaPaiFilhos, cache)) {
                allCovered = false;
                break;
            }
        }

        cache.put(codUnidade, allCovered);
        return allCovered;
    }

    private String calcularLinkDestinoProcesso(Processo processo, Perfil perfil, Long codigoUnidade) {
        if (perfil == Perfil.ADMIN && processo.getSituacao() == SituacaoProcesso.CRIADO) {
            return String.format("/processo/cadastro?codProcesso=%s", processo.getCodigo());
        }

        if (perfil == Perfil.ADMIN || perfil == Perfil.GESTOR) {
            return "/processo/" + processo.getCodigo();
        }

        var unidade = unidadeService.buscarPorCodigo(codigoUnidade);
        return String.format("/processo/%s/%s", processo.getCodigo(), unidade.getSigla());
    }
}
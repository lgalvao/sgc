package sgc.processo.painel;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.*;
import sgc.alerta.model.*;
import sgc.organizacao.*;
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
    public Page<ProcessoResumoDto> listarProcessos(ContextoUsuarioAutenticado contextoUsuario, Pageable pageable) {
        Perfil perfil = contextoUsuario.perfil();
        Long codigoUnidade = contextoUsuario.unidadeAtivaCodigo();
        Pageable sortedPageable = garantirOrdenacaoPadrao(pageable);
        Page<Processo> processos;
        String siglaUnidadeUsuario = obterSiglaUnidadeUsuario(perfil, codigoUnidade);

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
        return processos.map(processo -> paraProcessoResumoDto(processo, perfil, siglaUnidadeUsuario, mapaPaiFilhos));
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
    public Page<Alerta> listarAlertas(ContextoUsuarioAutenticado contextoUsuario, Pageable pageable) {
        Pageable sortedPageable = pageable.isPaged()
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "dataHora"))
                : pageable;

        Page<Alerta> alertasPage = alertaFacade.listarPorUnidade(contextoUsuario, sortedPageable);
        Map<Long, LocalDateTime> leiturasPorAlerta = alertaFacade.obterMapaDataHoraLeitura(
                contextoUsuario.usuarioTitulo(),
                alertasPage.stream().map(Alerta::getCodigo).toList());
        List<Long> alertasNaoLidosVisualizados = new ArrayList<>();
        alertasPage.forEach(alerta -> {
            Long codigoAlerta = alerta.getCodigo();
            LocalDateTime dataHoraLeitura = leiturasPorAlerta.get(codigoAlerta);
            if (dataHoraLeitura == null) {
                alertasNaoLidosVisualizados.add(codigoAlerta);
            }
            alerta.setDataHoraLeitura(dataHoraLeitura);
        });

        if (!alertasNaoLidosVisualizados.isEmpty()) {
            alertaFacade.marcarComoLidos(contextoUsuario, alertasNaoLidosVisualizados);
        }

        return alertasPage;
    }

    private ProcessoResumoDto paraProcessoResumoDto(Processo processo,
                                                    Perfil perfil,
                                                    @Nullable String siglaUnidadeUsuario,
                                                    Map<Long, List<Long>> mapaPaiFilhos) {

        var participantes = processo.getParticipantes();
        var participante = participantes.getFirst();

        Long codUnidMapeado = participante.getUnidadeCodigoPersistido();
        String nomeUnidMapeado = participante.getNome();

        String linkDestino = calcularLinkDestinoProcesso(processo, perfil, siglaUnidadeUsuario);
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
                .map(UnidadeProcesso::getUnidadeCodigoPersistido)
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

        Map<Long, String> existingSiglas = participantes.stream()
                .filter(participante -> participante.getSigla() != null && !participante.getSigla().isBlank())
                .collect(toMap(
                        UnidadeProcesso::getUnidadeCodigoPersistido,
                        UnidadeProcesso::getSigla,
                        (s1, s2) -> s1)
                );

        List<String> siglas = new ArrayList<>();
        List<Long> missingIds = new ArrayList<>();
        displayIds.forEach(codUnidade -> {
            String sigla = existingSiglas.get(codUnidade);
            if (sigla != null && !sigla.isBlank()) {
                siglas.add(sigla);
            } else {
                missingIds.add(codUnidade);
            }
        });

        if (!missingIds.isEmpty()) {
            siglas.addAll(unidadeService.buscarSiglasPorCodigos(missingIds));
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

    private String calcularLinkDestinoProcesso(Processo processo, Perfil perfil, @Nullable String siglaUnidadeUsuario) {
        if (perfil == Perfil.ADMIN && processo.getSituacao() == SituacaoProcesso.CRIADO) {
            return String.format("/processo/cadastro?codProcesso=%s", processo.getCodigo());
        }

        if (perfil == Perfil.ADMIN || perfil == Perfil.GESTOR) {
            return "/processo/" + processo.getCodigo();
        }

        if (siglaUnidadeUsuario == null || siglaUnidadeUsuario.isBlank()) {
            throw new IllegalStateException("Sigla da unidade do usuário ausente");
        }
        return String.format("/processo/%s/%s", processo.getCodigo(), siglaUnidadeUsuario);
    }

    private @Nullable String obterSiglaUnidadeUsuario(Perfil perfil, Long codigoUnidade) {
        if (perfil == Perfil.ADMIN || perfil == Perfil.GESTOR) {
            return null;
        }

        return unidadeService.buscarSiglaPorCodigo(codigoUnidade);
    }
}

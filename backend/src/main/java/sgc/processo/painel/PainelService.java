package sgc.processo.painel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaAplicacaoService;
import sgc.alerta.AlertaDtoMapper;
import sgc.alerta.model.Alerta;
import sgc.comum.erros.ErroInconsistenciaInterna;
import sgc.organizacao.ContextoUsuarioAutenticado;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.service.UnidadeHierarquiaService;
import sgc.organizacao.service.UnidadeService;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.UnidadeProcesso;
import sgc.processo.painel.dto.PainelBootstrapDto;
import sgc.processo.service.ProcessoService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PainelService {
    private final AlertaAplicacaoService alertaAplicacaoService;
    private final AlertaDtoMapper alertaDtoMapper;
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
            processos = processoService.listarIniciadosPorSubprocessos(codigosUnidades, sortedPageable);
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

    public Page<Alerta> listarAlertas(ContextoUsuarioAutenticado contextoUsuario, Pageable pageable) {
        Pageable sortedPageable = pageable.isPaged()
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "dataHora"))
                : pageable;

        Page<Alerta> alertasPage = alertaAplicacaoService.listarPorUnidade(contextoUsuario, sortedPageable);
        Map<Long, LocalDateTime> leiturasPorAlerta = alertaAplicacaoService.obterMapaDataHoraLeitura(
                contextoUsuario.usuarioTitulo(),
                alertasPage.stream().map(Alerta::getCodigo).toList());
        alertasPage.forEach(alerta -> alerta.setDataHoraLeitura(leiturasPorAlerta.get(alerta.getCodigo())));
        alertaAplicacaoService.aplicarPrazoConfiguradoLeituraAutomatica(alertasPage.getContent());
        return alertasPage;
    }

    @Transactional
    public void marcarAlertasLidos(ContextoUsuarioAutenticado contextoUsuario, List<Long> codigos) {
        if (codigos.isEmpty()) {
            return;
        }
        alertaAplicacaoService.marcarComoLidos(contextoUsuario, codigos);
    }

    private ProcessoResumoDto paraProcessoResumoDto(Processo processo,
                                                    Perfil perfil,
                                                    @Nullable String siglaUnidadeUsuario,
                                                    Map<Long, List<Long>> mapaPaiFilhos) {

        var participantes = processo.getParticipantes();
        UnidadeProcesso participante = participantes.isEmpty() ? null : participantes.getFirst();

        Long codUnidMapeado = participante != null ? participante.getUnidadeCodigoPersistido() : null;
        String nomeUnidMapeado = participante != null ? participante.getNome() : null;

        String linkDestino = calcularLinkDestinoProcesso(processo, perfil, siglaUnidadeUsuario);
        String unidadesParticipantes = participantes.isEmpty()
                ? ""
                : formatarUnidadesParticipantes(participantes, mapaPaiFilhos);

        return ProcessoResumoDto.builder()
                .codigo(processo.getCodigo())
                .descricao(processo.getDescricao())
                .situacao(processo.getSituacao() != null ? processo.getSituacao().name() : null)
                .tipo(processo.getTipo() != null ? processo.getTipo().name() : null)
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
            if (sigla != null) {
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
        if (children == null) {
            cache.put(codUnidade, false);
            return false;
        }
        if (children.isEmpty()) {
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
            throw new ErroInconsistenciaInterna("Sigla da unidade do usuário ausente");
        }
        return String.format("/processo/%s/%s", processo.getCodigo(), siglaUnidadeUsuario);
    }

    public PainelBootstrapDto obterBootstrap(ContextoUsuarioAutenticado contextoUsuario) {
        Page<ProcessoResumoDto> processos = listarProcessos(contextoUsuario, PageRequest.of(0, 10));
        Page<Alerta> alertas = listarAlertas(contextoUsuario, PageRequest.of(0, 200));

        return PainelBootstrapDto.builder()
                .processos(processos.getContent())
                .alertas(alertas.getContent().stream().map(alertaDtoMapper::paraAlertaDto).toList())
                .build();
    }

    private @Nullable String obterSiglaUnidadeUsuario(Perfil perfil, Long codigoUnidade) {
        if (perfil == Perfil.ADMIN || perfil == Perfil.GESTOR) {
            return null;
        }

        return unidadeService.buscarSiglaPorCodigo(codigoUnidade);
    }
}

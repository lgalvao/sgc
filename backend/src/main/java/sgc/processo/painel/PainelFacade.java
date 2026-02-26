package sgc.processo.painel;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.*;
import sgc.alerta.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PainelFacade {
    private final ProcessoFacade processoFacade;
    private final AlertaFacade alertaService;
    private final OrganizacaoFacade organizacaoFacade;

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

        // Pré-carrega o mapa da hierarquia para evitar N+1 selects
        Map<Long, List<Long>> mapaPaiFilhos = organizacaoFacade.buscarMapaHierarquia();

        if (perfil == Perfil.ADMIN) {
            processos = processoFacade.listarTodos(sortedPageable);
        } else {
            List<Long> codigosUnidades = new ArrayList<>();
            // GESTOR vê processos da unidade e subordinadas
            if (perfil == Perfil.GESTOR) {
                codigosUnidades.addAll(organizacaoFacade.buscarIdsDescendentes(codigoUnidade, mapaPaiFilhos));
            }
            // Os demais perfis veem apenas processos da própria unidade
            codigosUnidades.add(codigoUnidade);

            processos = processoFacade.listarPorParticipantesIgnorandoCriado(codigosUnidades, sortedPageable);
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

    /**
     * Lista alertas com base no usuário ou na unidade.
     *
     * <p>Os alertas são filtrados pela unidade fornecida.
     * O título do usuário é utilizado para verificar o status de leitura.
     */
    @Transactional
    public Page<Alerta> listarAlertas(String usuarioTitulo, Long codigoUnidade, Pageable pageable) {
        // Aplica ordenação padrão por dataHora decrescente para alertas também
        Pageable sortedPageable = pageable;
        if (pageable.isPaged() && pageable.getSort().isUnsorted()) {
            sortedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "dataHora"));
        }

        // Alertas são filtrados pela unidade do usuário (sem subordinadas)
        Page<Alerta> alertasPage = alertaService.listarPorUnidade(codigoUnidade, sortedPageable);
        List<Long> alertasNaoLidosVisualizados = new ArrayList<>();
        alertasPage.forEach(alerta -> {
            LocalDateTime dataHoraLeitura = alertaService.obterDataHoraLeitura(alerta.getCodigo(), usuarioTitulo).orElse(null);
            if (dataHoraLeitura == null) {
                alertasNaoLidosVisualizados.add(alerta.getCodigo());
            }
            alerta.setDataHoraLeitura(dataHoraLeitura);
        });

        if (!alertasNaoLidosVisualizados.isEmpty()) {
            alertaService.marcarComoLidos(usuarioTitulo, alertasNaoLidosVisualizados);
        }

        return alertasPage;
    }

    private ProcessoResumoDto paraProcessoResumoDto(Processo processo, Perfil perfil, Long codigoUnidade, Map<Long, List<Long>> mapaPaiFilhos) {
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

    private String formatarUnidadesParticipantes(List<UnidadeProcesso> participantes, Map<Long, List<Long>> mapaPaiFilhos) {
        Map<Long, Long> mapaFilhoPai = new HashMap<>();
        for (Map.Entry<Long, List<Long>> entry : mapaPaiFilhos.entrySet()) {
            Long pai = entry.getKey();
            for (Long filho : entry.getValue()) {
                mapaFilhoPai.put(filho, pai);
            }
        }

        Set<Long> participantesIds = participantes.stream()
                .map(UnidadeProcesso::getUnidadeCodigo)
                .collect(Collectors.toSet());

        Set<Long> displayIds = new HashSet<>();
        Map<Long, Boolean> coveredCache = new HashMap<>();

        for (Long pId : participantesIds) {
            Long candidate = pId;
            Long parent = mapaFilhoPai.get(candidate);

            while (parent != null) {
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
                .collect(Collectors.toMap(
                        UnidadeProcesso::getUnidadeCodigo,
                        UnidadeProcesso::getSigla,
                        (s1, s2) -> s1));

        List<String> siglas = new ArrayList<>();
        List<Long> missingIds = new ArrayList<>();

        for (Long id : displayIds) {
            if (existingSiglas.containsKey(id)) {
                siglas.add(existingSiglas.get(id));
            } else {
                missingIds.add(id);
            }
        }

        if (!missingIds.isEmpty()) {
            List<Unidade> missingUnidades = organizacaoFacade.unidadesPorCodigos(missingIds);
            missingUnidades.forEach(u -> siglas.add(u.getSigla()));
        }

        return siglas.stream().sorted().collect(Collectors.joining(", "));
    }

    private boolean isCovered(Long unidadeId, Set<Long> participantesIds, Map<Long, List<Long>> mapaPaiFilhos, Map<Long, Boolean> cache) {
        if (participantesIds.contains(unidadeId)) {
            return true;
        }
        if (cache.containsKey(unidadeId)) {
            return cache.get(unidadeId);
        }

        List<Long> children = mapaPaiFilhos.get(unidadeId);
        if (children == null || children.isEmpty()) {
            cache.put(unidadeId, false);
            return false;
        }

        boolean allCovered = true;
        for (Long child : children) {
            if (!isCovered(child, participantesIds, mapaPaiFilhos, cache)) {
                allCovered = false;
                break;
            }
        }

        cache.put(unidadeId, allCovered);
        return allCovered;
    }

    private String calcularLinkDestinoProcesso(Processo processo, Perfil perfil, Long codigoUnidade) {
        if (perfil == Perfil.ADMIN && processo.getSituacao() == SituacaoProcesso.CRIADO) {
            return String.format("/processo/cadastro?codProcesso=%s", processo.getCodigo());
        }
        if (perfil == Perfil.ADMIN || perfil == Perfil.GESTOR) {
            return "/processo/" + processo.getCodigo();
        }
        var unidade = organizacaoFacade.dtoPorCodigo(codigoUnidade);
        return String.format("/processo/%s/%s", processo.getCodigo(), unidade.getSigla());
    }
}

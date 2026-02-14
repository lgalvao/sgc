package sgc.painel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaFacade;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.model.Alerta;
import sgc.comum.util.FormatadorData;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Perfil;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.service.ProcessoFacade;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import sgc.processo.model.UnidadeProcesso;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PainelFacade {
    private final ProcessoFacade processoFacade;
    private final AlertaFacade alertaService;
    private final UnidadeFacade unidadeService;

    /**
     * Lista processos com base no perfil e na unidade do usuário.
     *
     * <p>Regras de visibilidade:
     * - ADMIN: vê todos os processos
     * - GESTOR: vê processos da unidade e de todas as subordinadas (recursivamente)
     * - CHEFE e SERVIDOR: veem processos APENAS da própria unidade
     * - Processos no estado 'CRIADO' são omitidos para perfis não-ADMIN
     *
     * @param perfil        O perfil do usuário (obrigatório).
     * @param codigoUnidade O código da unidade do usuário (necessário para perfis não-ADMIN).
     * @param pageable      As informações de paginação.
     * @return Uma página {@link Page} de {@link ProcessoResumoDto}.
     * @throws IllegalArgumentException se o perfil for nulo or em branco.
     */
    public Page<ProcessoResumoDto> listarProcessos(Perfil perfil, Long codigoUnidade, Pageable pageable) {
        Pageable sortedPageable = garantirOrdenacaoPadrao(pageable);
        Page<Processo> processos;

        // Pré-carrega o mapa da hierarquia para evitar N+1 selects
        Map<Long, List<Long>> mapaPaiFilhos = unidadeService.buscarMapaHierarquia();

        if (perfil == Perfil.ADMIN) {
            processos = processoFacade.listarTodos(sortedPageable);
        } else {
            List<Long> codigosUnidades = new ArrayList<>();
            // GESTOR vê processos da unidade e subordinadas
            if (perfil == Perfil.GESTOR) {
                codigosUnidades.addAll(unidadeService.buscarIdsDescendentes(codigoUnidade, mapaPaiFilhos));
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
     * <p>A busca prioriza o título do usuário. Se não for fornecido, busca pela unidade e suas
     * subordinadas. Se nenhum dos dois for fornecido, retorna todos os alertas.
     *
     * @param usuarioTitulo Título de eleitor do usuário.
     * @param codigoUnidade Código da unidade.
     * @param pageable      As informações de paginação.
     * @return Uma página {@link Page} de {@link AlertaDto}.
     */
    @Transactional
    public Page<AlertaDto> listarAlertas(String usuarioTitulo, Long codigoUnidade, Pageable pageable) {
        // Aplica ordenação padrão por dataHora decrescente para alertas também
        Pageable sortedPageable = pageable;
        if (pageable.isPaged() && pageable.getSort().isUnsorted()) {
            sortedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "dataHora"));
        }

        Page<Alerta> alertasPage;

        // Alertas são filtrados pela unidade do usuário (sem subordinadas)
        alertasPage = alertaService.listarPorUnidade(codigoUnidade, sortedPageable);

        List<Long> alertasNaoLidosVisualizados = new ArrayList<>();
        Page<AlertaDto> resultado = alertasPage.map(alerta -> {
            LocalDateTime dataHoraLeitura = alertaService.obterDataHoraLeitura(alerta.getCodigo(), usuarioTitulo).orElse(null);
            if (usuarioTitulo != null && dataHoraLeitura == null) {
                alertasNaoLidosVisualizados.add(alerta.getCodigo());
            }
            return paraAlertaDto(alerta, dataHoraLeitura);
        });

        if (usuarioTitulo != null && !alertasNaoLidosVisualizados.isEmpty()) {
            alertaService.marcarComoLidos(usuarioTitulo, alertasNaoLidosVisualizados);
        }

        return resultado;
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
                .situacaoLabel(processo.getSituacao() != null ? processo.getSituacao().getLabel() : "")
                .tipo(processo.getTipo().name())
                .tipoLabel(processo.getTipo().getLabel())
                .dataLimite(processo.getDataLimite())
                .dataLimiteFormatada(FormatadorData.formatarData(processo.getDataLimite()))
                .dataCriacao(processo.getDataCriacao())
                .dataFinalizacaoFormatada(FormatadorData.formatarData(processo.getDataFinalizacao()))
                .unidadeCodigo(codUnidMapeado)
                .unidadeNome(nomeUnidMapeado)
                .unidadesParticipantes(unidadesParticipantes)
                .linkDestino(linkDestino)
                .build();
    }

    private String formatarUnidadesParticipantes(List<UnidadeProcesso> participantes, Map<Long, List<Long>> mapaPaiFilhos) {
        Map<Long, UnidadeProcesso> participantesPorCodigo =
                participantes.stream().collect(Collectors.toMap(
                        UnidadeProcesso::getUnidadeCodigo,
                        up -> up,
                        (existing, replacement) -> existing));

        Set<Long> participantesIds = participantesPorCodigo.keySet();

        Set<Long> unidadesVisiveis = selecionarIdsVisiveis(participantesIds, participantesPorCodigo, mapaPaiFilhos);
        return unidadesVisiveis.stream()
                .map(participantesPorCodigo::get)
                .filter(Objects::nonNull)
                .map(UnidadeProcesso::getSigla)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    private Set<Long> selecionarIdsVisiveis(Set<Long> participantesIds, Map<Long, UnidadeProcesso> participantesPorCodigo, Map<Long, List<Long>> mapaPaiFilhos) {
        Set<Long> visiveis = new LinkedHashSet<>();
        for (Long unidadeId : participantesIds) {
            UnidadeProcesso unidade = participantesPorCodigo.get(unidadeId);
            Long candidato = encontrarMaiorIdVisivel(unidade, participantesIds, participantesPorCodigo, mapaPaiFilhos);
            visiveis.add(candidato);
        }
        return visiveis;
    }

    private Long encontrarMaiorIdVisivel(UnidadeProcesso unidade, Set<Long> participantesIds, 
            Map<Long, UnidadeProcesso> participantesPorCodigo, Map<Long, List<Long>> mapaPaiFilhos) {
        UnidadeProcesso atual = unidade;
        while (true) {
            if (!todasSubordinadasParticipam(atual.getUnidadeCodigo(), participantesIds, mapaPaiFilhos)) {
                return atual.getUnidadeCodigo();
            }
            Long superiorCodigo = atual.getUnidadeSuperiorCodigo();
            if (superiorCodigo == null || !participantesIds.contains(superiorCodigo)) {
                return atual.getUnidadeCodigo();
            }
            atual = participantesPorCodigo.get(superiorCodigo);
        }
    }

    private boolean todasSubordinadasParticipam(Long codigo, Set<Long> participantesIds, Map<Long, List<Long>> mapaPaiFilhos) {
        for (Long subordinadaId : unidadeService.buscarIdsDescendentes(codigo, mapaPaiFilhos)) {
            if (!participantesIds.contains(subordinadaId)) return false;
        }
        return true;
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

    private AlertaDto paraAlertaDto(Alerta alerta, LocalDateTime dataHoraLeitura) {
        String descricao = alerta.getDescricao();
        return AlertaDto.builder()
                .codigo(alerta.getCodigo())
                .codProcesso(alerta.getProcesso().getCodigo())
                .descricao(descricao)
                .dataHora(alerta.getDataHora())
                .dataHoraFormatada(FormatadorData.formatarDataHora(alerta.getDataHora()))
                .unidadeOrigem(alerta.getUnidadeOrigem().getSigla())
                .unidadeDestino(alerta.getUnidadeDestino().getSigla())
                .mensagem(descricao)
                .origem(alerta.getUnidadeOrigem().getSigla())
                .processo(alerta.getProcesso().getDescricao())
                .dataHoraLeitura(dataHoraLeitura)
                .build();
    }
}

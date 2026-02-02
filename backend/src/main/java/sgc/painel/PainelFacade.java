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
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.service.ProcessoFacade;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

        return alertasPage.map(
                alerta -> {
                    LocalDateTime dataHoraLeitura = alertaService.obterDataHoraLeitura(alerta.getCodigo(), usuarioTitulo).orElse(null);
                    return paraAlertaDto(alerta, dataHoraLeitura);
                });
    }

    private ProcessoResumoDto paraProcessoResumoDto(Processo processo, Perfil perfil, Long codigoUnidade, Map<Long, List<Long>> mapaPaiFilhos) {
        Set<Unidade> participantes = processo.getParticipantes();

        // As invariantes de BD e o @NullMarked garantem que participantes nunca é null e processos no painel têm ao menos um participante.
        Unidade participante = participantes.iterator().next();

        Long codUnidMapeado = participante.getCodigo();
        String nomeUnidMapeado = participante.getNome();

        String linkDestino = calcularLinkDestinoProcesso(processo, perfil, codigoUnidade);
        String unidadesParticipantes = formatarUnidadesParticipantes(participantes, mapaPaiFilhos);

        return ProcessoResumoDto.builder()
                .codigo(processo.getCodigo())
                .descricao(processo.getDescricao())
                .situacao(processo.getSituacao())
                .situacaoLabel(processo.getSituacao().getLabel())
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

    private String formatarUnidadesParticipantes(Set<Unidade> participantes, Map<Long, List<Long>> mapaPaiFilhos) {
        Map<Long, Unidade> participantesPorCodigo =
                participantes.stream().collect(Collectors.toMap(Unidade::getCodigo, unidade -> unidade));

        Set<Long> participantesIds = participantesPorCodigo.keySet();

        Set<Long> unidadesVisiveis = selecionarIdsVisiveis(participantesIds, participantesPorCodigo, mapaPaiFilhos);
        return unidadesVisiveis.stream()
                .map(participantesPorCodigo::get)
                .map(Unidade::getSigla)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    private Set<Long> selecionarIdsVisiveis(Set<Long> participantesIds, Map<Long, Unidade> participantesPorCodigo, Map<Long, List<Long>> mapaPaiFilhos) {
        Set<Long> visiveis = new LinkedHashSet<>();
        for (Long unidadeId : participantesIds) {
            Unidade unidade = participantesPorCodigo.get(unidadeId);
            Long candidato = encontrarMaiorIdVisivel(unidade, participantesIds, mapaPaiFilhos);
            visiveis.add(candidato);
        }
        return visiveis;
    }

    private Long encontrarMaiorIdVisivel(Unidade unidade, Set<Long> participantesIds, Map<Long, List<Long>> mapaPaiFilhos) {
        Unidade atual = unidade;
        while (true) {
            if (!todasSubordinadasParticipam(atual.getCodigo(), participantesIds, mapaPaiFilhos)) {
                return atual.getCodigo();
            }
            Unidade superior = atual.getUnidadeSuperior();
            if (superior == null || !participantesIds.contains(superior.getCodigo())) {
                return atual.getCodigo();
            }
            atual = superior;
        }
    }

    private boolean todasSubordinadasParticipam(Long codigo, Set<Long> participantesIds, Map<Long, List<Long>> mapaPaiFilhos) {
        for (Long subordinadaId : unidadeService.buscarIdsDescendentes(codigo, mapaPaiFilhos)) {
            if (!participantesIds.contains(subordinadaId)) return false;
        }
        return true;
    }

    private String calcularLinkDestinoProcesso(Processo processo, Perfil perfil, Long codigoUnidade) {
        try {
            if (perfil == Perfil.ADMIN && processo.getSituacao() == SituacaoProcesso.CRIADO) {
                return String.format("/processo/cadastro?codProcesso=%s", processo.getCodigo());
            }
            if (perfil == Perfil.ADMIN || perfil == Perfil.GESTOR) {
                return "/processo/" + processo.getCodigo();
            }
            var unidade = unidadeService.buscarPorCodigo(codigoUnidade);
            return String.format("/processo/%s/%s", processo.getCodigo(), unidade.getSigla());
        } catch (Exception e) {
            log.warn("Erro ao calcular link de destino para o processo {}: {}", processo.getCodigo(), e.getMessage());
            return "";
        }
    }

    private AlertaDto paraAlertaDto(Alerta alerta, LocalDateTime dataHoraLeitura) {
        return AlertaDto.builder()
                .codigo(alerta.getCodigo())
                .codProcesso(alerta.getProcesso().getCodigo())
                .descricao(alerta.getDescricao())
                .dataHora(alerta.getDataHora())
                .unidadeOrigem(alerta.getUnidadeOrigem().getSigla())
                .unidadeDestino(alerta.getUnidadeDestino().getSigla())
                .dataHoraLeitura(dataHoraLeitura)
                .build();
    }
}

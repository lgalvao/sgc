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

        if (perfil == Perfil.ADMIN) {
            processos = processoFacade.listarTodos(sortedPageable);
        } else {
            List<Long> codigosUnidades = new ArrayList<>();
            // GESTOR vê processos da unidade e subordinadas
            if (perfil == Perfil.GESTOR) {
                codigosUnidades.addAll(unidadeService.buscarIdsDescendentes(codigoUnidade));
            }
            // Os demais perfis veem apenas processos da própria unidade
            codigosUnidades.add(codigoUnidade);

            processos = processoFacade.listarPorParticipantesIgnorandoCriado(codigosUnidades, sortedPageable);
        }
        return processos.map(processo -> paraProcessoResumoDto(processo, perfil, codigoUnidade));
    }

    private Pageable garantirOrdenacaoPadrao(Pageable pageable) {
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
                    LocalDateTime dataHoraLeitura = alertaService
                            .obterDataHoraLeitura(alerta.getCodigo(), usuarioTitulo)
                            .orElse(null);
                    return paraAlertaDto(alerta, dataHoraLeitura);
                });
    }

    private ProcessoResumoDto paraProcessoResumoDto(Processo processo, Perfil perfil, Long codigoUnidade) {
        Set<Unidade> participantes = processo.getParticipantes();
        Unidade participante = participantes.iterator().next();

        String linkDestino = calcularLinkDestinoProcesso(processo, perfil, codigoUnidade);
        String unidadesParticipantes = formatarUnidadesParticipantes(processo.getParticipantes());

        return ProcessoResumoDto.builder()
                .codigo(processo.getCodigo())
                .descricao(processo.getDescricao())
                .situacao(processo.getSituacao())
                .tipo(processo.getTipo().name())
                .dataLimite(processo.getDataLimite())
                .dataCriacao(processo.getDataCriacao())
                .unidadeCodigo(participante.getCodigo())
                .unidadeNome(participante.getNome())
                .unidadesParticipantes(unidadesParticipantes)
                .linkDestino(linkDestino)
                .build();
    }

    private String formatarUnidadesParticipantes(Set<Unidade> participantes) {
        if (participantes.isEmpty()) return "";

        Map<Long, Unidade> participantesPorCodigo =
                participantes.stream().collect(Collectors.toMap(Unidade::getCodigo, unidade -> unidade));

        Set<Long> participantesIds = participantesPorCodigo.keySet();

        Set<Long> unidadesVisiveis = selecionarIdsVisiveis(participantesIds, participantesPorCodigo);
        return unidadesVisiveis.stream()
                .map(participantesPorCodigo::get)
                .map(Unidade::getSigla)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    private Set<Long> selecionarIdsVisiveis(Set<Long> participantesIds, Map<Long, Unidade> participantesPorCodigo) {
        Set<Long> visiveis = new LinkedHashSet<>();
        for (Long unidadeId : participantesIds) {
            Unidade unidade = participantesPorCodigo.get(unidadeId);
            Long candidato = encontrarMaiorIdVisivel(unidade, participantesIds);
            visiveis.add(candidato);
        }
        return visiveis;
    }

    private Long encontrarMaiorIdVisivel(Unidade unidade, Set<Long> participantesIds) {
        Unidade atual = unidade;
        while (true) {
            if (!todasSubordinadasParticipam(atual.getCodigo(), participantesIds)) {
                return atual.getCodigo();
            }
            Unidade superior = atual.getUnidadeSuperior();
            if (superior == null || !participantesIds.contains(superior.getCodigo())) {
                return atual.getCodigo();
            }
            atual = superior;
        }
    }

    private boolean todasSubordinadasParticipam(Long codigo, Set<Long> participantesIds) {
        for (Long subordinadaId : unidadeService.buscarIdsDescendentes(codigo)) {
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
            if (codigoUnidade == null) {
                return null;
            }
            var unidade = unidadeService.buscarPorCodigo(codigoUnidade);
            return String.format("/processo/%s/%s", processo.getCodigo(), (unidade != null) ? unidade.getSigla() : "null");
        } catch (Exception e) {
            log.warn("Erro ao calcular link de destino para o processo {}: {}", processo.getCodigo(), e.getMessage());
            return null;
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

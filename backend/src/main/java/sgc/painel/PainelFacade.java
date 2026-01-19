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
import org.jspecify.annotations.Nullable;
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
     */
    public Page<ProcessoResumoDto> listarProcessos(Perfil perfil, Long codigoUnidade, Pageable pageable) {
        Pageable sortedPageable = pageable.isPaged() ? garantirOrdenacaoPadrao(pageable) : pageable;
        Page<Processo> processos;

        if (perfil == Perfil.ADMIN) {
            processos = processoFacade.listarTodos(sortedPageable);
        } else {
            List<Long> unidadeIds = new ArrayList<>();
            
            // GESTOR vê processos da unidade e subordinadas
            if (perfil == Perfil.GESTOR) {
                unidadeIds.addAll(unidadeService.buscarIdsDescendentes(codigoUnidade));
            }
            
            // Todos os perfis veem processos da própria unidade
            unidadeIds.add(codigoUnidade);
            processos = processoFacade.listarPorParticipantesIgnorandoCriado(unidadeIds, sortedPageable);
        }
        return processos != null ? processos.map(processo -> paraProcessoResumoDto(processo, perfil, codigoUnidade)) : Page.empty();
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

        // Alertas são filtrados pela unidade do usuário (sem subordinadas)
        Page<Alerta> alertasPage = alertaService.listarPorUnidade(codigoUnidade, sortedPageable);

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

            if (candidato != null) visiveis.add(candidato);
        }
        return visiveis;
    }

    @Nullable
    private Long encontrarMaiorIdVisivel(@org.jspecify.annotations.Nullable Unidade unidade, Set<Long> participantesIds) {
        if (unidade == null || !participantesIds.contains(unidade.getCodigo())) return null;
        Unidade atual = unidade;
        while (true) {
            if (!todasSubordinadasParticipam(atual.getCodigo(), participantesIds)) return atual.getCodigo();
            Unidade superior = atual.getUnidadeSuperior();
            if (superior == null || !participantesIds.contains(superior.getCodigo())) return atual.getCodigo();
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
        if (perfil == Perfil.ADMIN && processo.getSituacao() == SituacaoProcesso.CRIADO) {
            return String.format("/processo/cadastro?codProcesso=%s", processo.getCodigo());
        }

        if (perfil == Perfil.ADMIN || perfil == Perfil.GESTOR) {
            return "/processo/" + processo.getCodigo();
        }
        
        try {
            var unidade = unidadeService.buscarPorCodigo(codigoUnidade);
            return String.format("/processo/%s/%s", processo.getCodigo(), unidade.getSigla());
        } catch (Exception e) {
            return "/processo/" + processo.getCodigo();
        }
    }

    private AlertaDto paraAlertaDto(Alerta alerta, LocalDateTime dataHoraLeitura) {
        return AlertaDto.builder()
                .codigo(alerta.getCodigo())
                .codProcesso(alerta.getProcesso() != null ? alerta.getProcesso().getCodigo() : null)
                .descricao(alerta.getDescricao())
                .dataHora(alerta.getDataHora())
                .unidadeOrigem(alerta.getUnidadeOrigem() != null ? alerta.getUnidadeOrigem().getSigla() : null)
                .unidadeDestino(alerta.getUnidadeDestino() != null ? alerta.getUnidadeDestino().getSigla() : null)
                .dataHoraLeitura(dataHoraLeitura)
                .build();
    }
}

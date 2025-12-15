package sgc.painel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuario;
import sgc.alerta.model.AlertaUsuarioRepo;
import sgc.painel.erros.ErroParametroPainelInvalido;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.sgrh.model.Perfil;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PainelService {
    private final ProcessoRepo processoRepo;
    private final AlertaRepo alertaRepo;
    private final AlertaUsuarioRepo alertaUsuarioRepo;
    private final UnidadeRepo unidadeRepo;

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
    public Page<ProcessoResumoDto> listarProcessos(
            Perfil perfil, Long codigoUnidade, Pageable pageable) {
        if (perfil == null) {
            throw new ErroParametroPainelInvalido("O parâmetro 'perfil' é obrigatório");
        }

        // Garante ordenação padrão por data de criação decrescente se nenhuma for fornecida
        // Isso resolve o problema onde processos recém-criados ("Rev 10") ficavam escondidos
        // dependendo da ordem aleatória do banco ou paginação.
        Pageable sortedPageable = garantirOrdenacaoPadrao(pageable);

        Page<Processo> processos;
        if (perfil == Perfil.ADMIN) {
            processos = processoRepo.findAll(sortedPageable);
        } else {
            if (codigoUnidade == null) return Page.empty(sortedPageable);

            List<Long> unidadeIds = new ArrayList<>();
            
            // GESTOR vê processos da unidade e subordinadas
            if (perfil == Perfil.GESTOR) {
                unidadeIds.addAll(obterIdsUnidadesSubordinadas(codigoUnidade));
            }
            
            // Todos os perfis veem processos da própria unidade
            unidadeIds.add(codigoUnidade);

            processos = processoRepo.findDistinctByParticipantes_CodigoInAndSituacaoNot(
                    unidadeIds, SituacaoProcesso.CRIADO, sortedPageable);
        }
        return processos.map(processo -> paraProcessoResumoDto(processo, perfil, codigoUnidade));
    }

    private Pageable garantirOrdenacaoPadrao(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "dataCriacao")
                            .and(Sort.by(Sort.Direction.DESC, "codigo")));
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
        if (pageable.getSort().isUnsorted()) {
            sortedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "dataHora"));
        }

        Page<Alerta> alertasPage;
    
        if (usuarioTitulo != null && !usuarioTitulo.isBlank()) {
            alertasPage = alertaRepo.findByUsuarioDestino_TituloEleitoral(usuarioTitulo, sortedPageable);
        } else if (codigoUnidade != null) {
            List<Long> unidadeIds = obterIdsUnidadesSubordinadas(codigoUnidade);
            unidadeIds.add(codigoUnidade);
            alertasPage = alertaRepo.findByUnidadeDestino_CodigoIn(unidadeIds, sortedPageable);
        } else {
            alertasPage = alertaRepo.findAll(sortedPageable);
        }

        return alertasPage.map(
                alerta -> {
                    LocalDateTime dataHoraLeitura = null;
                    if (usuarioTitulo != null && !usuarioTitulo.isBlank()) {
                        dataHoraLeitura = alertaUsuarioRepo
                                        .findById(new AlertaUsuario.Chave(alerta.getCodigo(), usuarioTitulo))
                                        .map(AlertaUsuario::getDataHoraLeitura)
                                        .orElse(null);
                    }
                    return paraAlertaDto(alerta, dataHoraLeitura);
                });
    }

    private List<Long> obterIdsUnidadesSubordinadas(Long codUnidade) {
        List<Unidade> subordinadas = unidadeRepo.findByUnidadeSuperiorCodigo(codUnidade);
        List<Long> ids = new ArrayList<>();
        for (Unidade u : subordinadas) {
            ids.add(u.getCodigo());
            ids.addAll(obterIdsUnidadesSubordinadas(u.getCodigo()));
        }
        return ids;
    }

    private ProcessoResumoDto paraProcessoResumoDto(Processo processo, Perfil perfil, Long codigoUnidade) {
        Unidade participante = processo.getParticipantes() != null && !processo.getParticipantes().isEmpty()
                ? processo.getParticipantes().iterator().next()
                : null;
        String linkDestino = calcularLinkDestinoProcesso(processo, perfil, codigoUnidade);
        String unidadesParticipantes = formatarUnidadesParticipantes(processo.getParticipantes());

        return ProcessoResumoDto.builder()
                .codigo(processo.getCodigo())
                .descricao(processo.getDescricao())
                .situacao(processo.getSituacao())
                .tipo(processo.getTipo().name())
                .dataLimite(processo.getDataLimite())
                .dataCriacao(processo.getDataCriacao())
                .unidadeCodigo(participante != null ? participante.getCodigo() : null)
                .unidadeNome(participante != null ? participante.getNome() : null)
                .unidadesParticipantes(unidadesParticipantes)
                .linkDestino(linkDestino)
                .build();
    }

    private String formatarUnidadesParticipantes(Set<Unidade> participantes) {
        if (participantes == null || participantes.isEmpty()) return "";

        Map<Long, Unidade> participantesPorCodigo =
                participantes.stream().collect(Collectors.toMap(Unidade::getCodigo, unidade -> unidade));

        Set<Long> participantesIds = participantesPorCodigo.keySet();

        Set<Long> unidadesVisiveis = selecionarIdsVisiveis(participantesIds);
        return unidadesVisiveis.stream()
                .map(participantesPorCodigo::get)
                .filter(unidade -> unidade != null && unidade.getSigla() != null)
                .map(Unidade::getSigla)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    private Set<Long> selecionarIdsVisiveis(Set<Long> participantesIds) {
        Set<Long> visiveis = new LinkedHashSet<>();
        for (Long unidadeId : participantesIds) {
            Unidade unidade = unidadeRepo.findById(unidadeId).orElse(null);
            Long candidato = encontrarMaiorIdVisivel(unidade, participantesIds);
            if (candidato != null) visiveis.add(candidato);
        }
        return visiveis;
    }

    private Long encontrarMaiorIdVisivel(Unidade unidade, Set<Long> participantesIds) {
        if (unidade == null || !participantesIds.contains(unidade.getCodigo())) {
            return null;
        }
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
        for (Long subordinadaId : obterIdsUnidadesSubordinadas(codigo)) {
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
        // Para CHEFE ou SERVIDOR, precisamos da sigla da unidade
        if (codigoUnidade != null) {
            return unidadeRepo.findById(codigoUnidade)
                    .map(unidade -> String.format("/processo/%s/%s", processo.getCodigo(), unidade.getSigla()))
                    .orElse(null);
        }
        return null;
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

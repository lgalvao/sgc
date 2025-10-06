package sgc.subprocesso;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.Atividade;
import sgc.atividade.AtividadeRepository;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.conhecimento.Conhecimento;
import sgc.conhecimento.ConhecimentoDTO;
import sgc.conhecimento.ConhecimentoMapper;
import sgc.conhecimento.ConhecimentoRepository;
import sgc.notificacao.Notificacao;
import sgc.notificacao.NotificacaoRepository;
import sgc.notificacao.NotificationService;
import sgc.processo.EventoSubprocessoDisponibilizado;
import sgc.unidade.Unidade;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/**
 * Serviço responsável por recuperar os detalhes de um Subprocesso (CDU-07).
 * - monta SubprocessoDetalheDTO com unidade, responsável, situação, localização atual,
 * prazo da etapa atual, movimentações (ordenadas) e elementos do processo (atividades/conhecimentos).
 * - valida permissão básica por perfil/unidade (ADMIN | GESTOR).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoService {
    private final SubprocessoRepository subprocessoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final AtividadeRepository atividadeRepository;
    private final ConhecimentoRepository conhecimentoRepository;
    private final AnaliseCadastroRepository analiseCadastroRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Recupera os detalhes do subprocesso.
     *
     * @param id             id do subprocesso
     * @param perfil         perfil do usuário (ADMIN | GESTOR)
     * @param unidadeUsuario código da unidade do usuário (pode ser null para ADMIN)
     * @return SubprocessoDetalheDTO montado
     * @throws ErroDominioNaoEncontrado se subprocesso não existir
     * @throws ErroDominioAccessoNegado se usuário não tiver permissão para acessar
     */
    @Transactional(readOnly = true)
    public SubprocessoDetalheDTO obterDetalhes(Long id, String perfil, Long unidadeUsuario) {
        if (perfil == null) {
            throw new ErroDominioAccessoNegado("Perfil inválido para acesso aos detalhes do subprocesso.");
        }

        Subprocesso sp = subprocessoRepository.findById(id)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(id)));

        // autorização: ADMIN tem acesso; GESTOR apenas se sua unidade for a unidade do subprocesso
        if ("GESTOR".equalsIgnoreCase(perfil)) {
            if (sp.getUnidade() == null || unidadeUsuario == null || !unidadeUsuario.equals(sp.getUnidade().getCodigo())) {
                throw new ErroDominioAccessoNegado("Usuário sem permissão para visualizar este subprocesso.");
            }
        } else if (!"ADMIN".equalsIgnoreCase(perfil) && !"GESTOR".equalsIgnoreCase(perfil)) {
            throw new ErroDominioAccessoNegado("Perfil sem permissão.");
        }

        // movimentações ordenadas (mais recente primeiro)
        List<Movimentacao> movimentacoes = movimentacaoRepository.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());

        // atividades do mapa do subprocesso (se existir mapa)
        final List<Atividade> atividades = (sp.getMapa() != null && sp.getMapa().getCodigo() != null)
                ? atividadeRepository.findByMapaCodigo(sp.getMapa().getCodigo())
                : emptyList();

        // conhecimentos vinculados às atividades — construir conjunto de ids para filtrar eficientemente
        final Set<Long> atividadeIds = atividades.stream()
                .map(Atividade::getCodigo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Conhecimento> conhecimentos = conhecimentoRepository.findAll()
                .stream()
                .filter(c -> c.getAtividade() != null && atividadeIds.contains(c.getAtividade().getCodigo()))
                .collect(Collectors.toList());

        // montar DTO final delegando parte da conversão ao mapeamento
        return SubprocessoMapper.toDetailDTO(sp, movimentacoes, atividades, conhecimentos);
    }

    /**
     * Retorna DTO agregado com atividades e conhecimentos do cadastro do subprocesso.
     * <p>
     * Estrutura retornada:
     * SubprocessoCadastroDTO { subprocessoId, unidadeSigla, atividades:[AtividadeCadastroDTO{id, descricao, conhecimentos:[ConhecimentoDTO...]}...] }
     *
     * @param subprocessoId id do subprocesso
     * @return SubprocessoCadastroDTO com dados do cadastro
     */
    @Transactional(readOnly = true)
    public SubprocessoCadastroDTO obterCadastro(Long subprocessoId) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        List<SubprocessoCadastroDTO.AtividadeCadastroDTO> atividadesComConhecimentos = new ArrayList<>();
        if (sp.getMapa() != null && sp.getMapa().getCodigo() != null) {
            List<Atividade> atividades = atividadeRepository.findByMapaCodigo(sp.getMapa().getCodigo());
            if (atividades == null) atividades = emptyList();

            for (Atividade a : atividades) {
                List<Conhecimento> ks = conhecimentoRepository.findByAtividadeCodigo(a.getCodigo());
                List<ConhecimentoDTO> ksDto = ks == null
                        ? emptyList()
                        : ks.stream().map(ConhecimentoMapper::toDTO).toList();

                atividadesComConhecimentos.add(new SubprocessoCadastroDTO.AtividadeCadastroDTO(
                        a.getCodigo(),
                        a.getDescricao(),
                        ksDto
                ));
            }
        }

        return new SubprocessoCadastroDTO(
                sp.getCodigo(),
                sp.getUnidade() != null ? sp.getUnidade().getSigla() : null,
                atividadesComConhecimentos
        );
    }

    /**
     * Retorna as atividades do mapa do subprocesso que não possuem conhecimentos associados.
     *
     * @param subprocessoId id do subprocesso
     * @return lista de atividades sem conhecimento (vazia se todas possuem)
     */
    @Transactional(readOnly = true)
    public List<Atividade> obterAtividadesSemConhecimento(Long subprocessoId) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        if (sp.getMapa() == null || sp.getMapa().getCodigo() == null) {
            return emptyList();
        }

        List<Atividade> atividades = atividadeRepository.findByMapaCodigo(sp.getMapa().getCodigo());
        if (atividades == null || atividades.isEmpty()) {
            return emptyList();
        }

        return atividades.stream()
                .filter(a -> {
                    if (a.getCodigo() == null) return true;
                    List<Conhecimento> ks = conhecimentoRepository.findByAtividadeCodigo(a.getCodigo());
                    return ks == null || ks.isEmpty();
                })
                .collect(Collectors.toList());
    }

    /**
     * Realiza as ações necessárias para disponibilizar o cadastro de atividades:
     * - validações (já realizadas pelo controle)
     * - atualiza situação do subprocesso
     * - registra movimentação
     * - define data_fim_etapa1
     * - remove histórico de análise (ANALISE_CADASTRO)
     * - persiste notificação e dispara alerta via NotificationService
     *
     * @param subprocessoId id do subprocesso
     */
    @Transactional
    public void disponibilizarCadastroAcao(Long subprocessoId) {
        log.info("Iniciando disponibilização do cadastro para subprocesso {}", subprocessoId);
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        // Validações básicas de pré-condição
        if (sp.getMapa() == null || sp.getMapa().getCodigo() == null) {
            log.warn("Validação falhou: subprocesso {} não possui mapa associado", subprocessoId);
            throw new IllegalStateException("Subprocesso sem mapa associado");
        }

        // mudar situação
        sp.setSituacaoId("CADASTRO_DISPONIBILIZADO");
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        subprocessoRepository.save(sp);

        // determinar unidade destino (unidade superior preferencial)
        Unidade unidadeSuperior = null;
        if (sp.getUnidade() != null) {
            unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        }
        if (unidadeSuperior == null) {
            List<Movimentacao> movs = movimentacaoRepository.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
            if (movs != null && !movs.isEmpty()) {
                unidadeSuperior = movs.getFirst().getUnidadeDestino();
            }
        }

        // registrar movimentação
        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade());
        mov.setUnidadeDestino(unidadeSuperior);
        mov.setDescricao("Disponibilização do cadastro de atividades");
        movimentacaoRepository.save(mov);

        // remover histórico de análise do cadastro
        if (analiseCadastroRepository != null) {
            analiseCadastroRepository.deleteBySubprocessoCodigo(sp.getCodigo());
        }

        // persistir notificação
        if (notificacaoRepository != null) {
            Notificacao n = new Notificacao();
            n.setSubprocesso(sp);
            n.setDataHora(java.time.LocalDateTime.now());
            n.setUnidadeOrigem(sp.getUnidade());
            n.setUnidadeDestino(unidadeSuperior);
            n.setConteudo("Cadastro de atividades e conhecimentos da unidade " + (sp.getUnidade() != null ? sp.getUnidade().getSigla() : "") + " disponibilizado para análise");
            notificacaoRepository.save(n);
        }

        // disparar notificação externa via NotificationService (mock em perfil de teste)
        try {
            if (notificationService != null && unidadeSuperior != null) {
                notificationService.enviarEmail(
                        unidadeSuperior.getSigla(),
                        "Cadastro disponibilizado",
                        "Cadastro de atividades disponibilizado para análise"
                );
            }
        } catch (Exception ignored) {
        }

        // publicar evento de domínio indicando sucesso da operação
        try {
            eventPublisher.publishEvent(new EventoSubprocessoDisponibilizado(sp.getCodigo()));
            log.info("Evento EventoSubprocessoDisponibilizado publicado para subprocesso {}", subprocessoId);
        } catch (Exception ex) {
            log.warn("Falha ao publicar evento EventoSubprocessoDisponibilizado para subprocesso {}: {}",
                    subprocessoId,
                    ex.getMessage()
            );
        }

        log.info("Disponibilização do cadastro concluída com sucesso para subprocesso {}", subprocessoId);
    }

    /**
     * Realiza as ações necessárias para disponibilizar a revisão do cadastro de atividades.
     *
     * @param subprocessoId id do subprocesso
     */
    @Transactional
    public void disponibilizarRevisaoAcao(Long subprocessoId) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        sp.setSituacaoId("REVISAO_CADASTRO_DISPONIBILIZADA");
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        subprocessoRepository.save(sp);

        Unidade unidadeSuperior = null;
        if (sp.getUnidade() != null) {
            unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        }
        if (unidadeSuperior == null) {
            List<Movimentacao> movs = movimentacaoRepository.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
            if (movs != null && !movs.isEmpty()) {
                unidadeSuperior = movs.getFirst().getUnidadeDestino();
            }
        }

        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade());
        mov.setUnidadeDestino(unidadeSuperior);
        mov.setDescricao("Disponibilização da revisão do cadastro de atividades");
        movimentacaoRepository.save(mov);

        if (analiseCadastroRepository != null) {
            analiseCadastroRepository.deleteBySubprocessoCodigo(sp.getCodigo());
        }

        if (notificacaoRepository != null) {
            Notificacao n = new Notificacao();
            n.setSubprocesso(sp);
            n.setDataHora(java.time.LocalDateTime.now());
            n.setUnidadeOrigem(sp.getUnidade());
            n.setUnidadeDestino(unidadeSuperior);
            n.setConteudo("Revisão do cadastro de atividades da unidade %s disponibilizada para análise"
                    .formatted(sp.getUnidade() != null ? sp.getUnidade().getSigla() : ""));
            notificacaoRepository.save(n);
        }

        try {
            if (notificationService != null && unidadeSuperior != null) {
                notificationService.enviarEmail(unidadeSuperior.getSigla(),
                        "Revisão do cadastro disponibilizada",
                        "Revisão do cadastro de atividades disponibilizada para análise"
                );
            }
        } catch (Exception ignored) {
        }
    }
}
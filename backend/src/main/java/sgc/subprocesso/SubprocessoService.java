package sgc.subprocesso;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.Alerta;
import sgc.alerta.AlertaRepository;
import sgc.atividade.AnaliseCadastro;
import sgc.atividade.AnaliseValidacao;
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
import sgc.subprocesso.dto.AnaliseValidacaoDTO;
import sgc.subprocesso.dto.MapaAjusteDTO;
import sgc.subprocesso.dto.SugestoesDTO;
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
    private final AnaliseValidacaoRepository analiseValidacaoRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;
    private final AlertaRepository alertaRepository;

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

    /**
     * Disponibiliza o mapa de competências para validação (CDU-17).
     */
    @Transactional
    public SubprocessoDTO disponibilizarMapa(Long subprocessoId, String observacoes, java.time.LocalDate dataLimiteEtapa2, String usuarioTitulo) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        // Validações
        if (sp.getMapa() == null) {
            throw new IllegalStateException("Subprocesso sem mapa associado");
        }

        // Atualizar situação e datas
        sp.setSituacaoId("MAPA_DISPONIBILIZADO");
        sp.setDataLimiteEtapa2(dataLimiteEtapa2);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        subprocessoRepository.save(sp);

        // Registrar movimentação
        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade());
        mov.setUnidadeDestino(sp.getUnidade()); // permanece na mesma unidade
        mov.setDescricao("Disponibilização do mapa de competências para validação");
        movimentacaoRepository.save(mov);

        return SubprocessoMapper.toDTO(sp);
    }

    /**
     * Apresenta sugestões para o mapa (CDU-19).
     */
    @Transactional
    public SubprocessoDTO apresentarSugestoes(Long subprocessoId, String sugestoes, String usuarioTitulo) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        // Atualizar mapa com sugestões
        if (sp.getMapa() != null) {
            sp.getMapa().setSugestoes(sugestoes);
        }
        sp.setSituacaoId("MAPA_COM_SUGESTOES");
        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        subprocessoRepository.save(sp);

        // Registrar movimentação
        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade());
        mov.setUnidadeDestino(sp.getUnidade().getUnidadeSuperior());
        mov.setDescricao("Apresentação de sugestões para o mapa de competências");
        movimentacaoRepository.save(mov);

        // Limpar histórico de análise
        if (analiseValidacaoRepository != null) {
            analiseValidacaoRepository.deleteBySubprocesso_Codigo(sp.getCodigo());
        }

        // Notificação e alerta
        notificarSugestoes(sp);

        return SubprocessoMapper.toDTO(sp);
    }

    /**
     * Valida o mapa de competências (CDU-19).
     */
    @Transactional
    public SubprocessoDTO validarMapa(Long subprocessoId, String usuarioTitulo) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        sp.setSituacaoId("MAPA_VALIDADO");
        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        subprocessoRepository.save(sp);

        // Registrar movimentação
        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade());
        mov.setUnidadeDestino(sp.getUnidade().getUnidadeSuperior());
        mov.setDescricao("Validação do mapa de competências");
        movimentacaoRepository.save(mov);

        // Limpar histórico de análise
        if (analiseValidacaoRepository != null) {
            analiseValidacaoRepository.deleteBySubprocesso_Codigo(sp.getCodigo());
        }

        // Notificação e alerta
        notificarValidacao(sp);

        return SubprocessoMapper.toDTO(sp);
    }

    /**
     * Obtém as sugestões do mapa.
     */
    @Transactional(readOnly = true)
    public SugestoesDTO obterSugestoes(Long subprocessoId) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        String sugestoes = sp.getMapa() != null ? sp.getMapa().getSugestoes() : null;
        boolean sugestoesApresentadas = sugestoes != null && !sugestoes.trim().isEmpty();
        String unidadeNome = sp.getUnidade() != null ? sp.getUnidade().getNome() : null;
        return new SugestoesDTO(sugestoes, sugestoesApresentadas, unidadeNome);
    }

    /**
     * Obtém o histórico de validação.
     */
    @Transactional(readOnly = true)
    public List<AnaliseValidacaoDTO> obterHistoricoValidacao(Long subprocessoId) {
        return analiseValidacaoRepository.findBySubprocesso_CodigoOrderByDataHoraDesc(subprocessoId)
                .stream()
                .map(this::mapearParaDTO)
                .collect(Collectors.toList());
    }

    /**
     * Devolve a validação para ajustes (CDU-20).
     */
    @Transactional
    public SubprocessoDTO devolverValidacao(Long subprocessoId, String justificativa, String usuarioTitulo) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        // Registrar análise
        AnaliseValidacao analise = new AnaliseValidacao();
        analise.setSubprocesso(sp);
        analise.setDataHora(java.time.LocalDateTime.now());
        analise.setObservacoes(justificativa);
        analiseValidacaoRepository.save(analise);

        // Identificar unidade de devolução
        Unidade unidadeDevolucao = sp.getUnidade();

        // Registrar movimentação
        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade().getUnidadeSuperior());
        mov.setUnidadeDestino(unidadeDevolucao);
        mov.setDescricao("Devolução da validação do mapa de competências para ajustes");
        movimentacaoRepository.save(mov);

        // Atualizar situação
        sp.setSituacaoId("MAPA_DISPONIBILIZADO");
        sp.setDataFimEtapa2(null);
        subprocessoRepository.save(sp);

        // Notificação e alerta
        notificarDevolucao(sp, unidadeDevolucao);

        return SubprocessoMapper.toDTO(sp);
    }

    /**
     * Aceita a validação (CDU-20).
     */
    @Transactional
    public SubprocessoDTO aceitarValidacao(Long subprocessoId, String usuarioTitulo) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        // Registrar análise
        AnaliseValidacao analise = new AnaliseValidacao();
        analise.setSubprocesso(sp);
        analise.setDataHora(java.time.LocalDateTime.now());
        analise.setObservacoes("Aceite da validação");
        analiseValidacaoRepository.save(analise);

        // Registrar movimentação
        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade().getUnidadeSuperior());
        mov.setUnidadeDestino(sp.getUnidade().getUnidadeSuperior().getUnidadeSuperior());
        mov.setDescricao("Mapa de competências validado");
        movimentacaoRepository.save(mov);

        // Notificação e alerta
        notificarAceite(sp);

        return SubprocessoMapper.toDTO(sp);
    }

    /**
     * Homologa a validação (CDU-20).
     */
    @Transactional
    public SubprocessoDTO homologarValidacao(Long subprocessoId, String usuarioTitulo) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        sp.setSituacaoId("MAPA_HOMOLOGADO");
        subprocessoRepository.save(sp);

        return SubprocessoMapper.toDTO(sp);
    }

    /**
     * Obtém mapa para ajuste (CDU-16).
     */
    @Transactional(readOnly = true)
    public MapaAjusteDTO obterMapaParaAjuste(Long subprocessoId) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        if (sp.getMapa() == null) {
            throw new IllegalStateException("Subprocesso sem mapa");
        }

        // Implementar lógica para montar MapaAjusteDTO
        Long mapaId = sp.getMapa().getCodigo();
        String unidadeNome = sp.getUnidade() != null ? sp.getUnidade().getNome() : "";
        List<sgc.subprocesso.dto.CompetenciaAjusteDTO> competencias = List.of(); // Placeholder
        String justificativa = null; // Placeholder
        return new MapaAjusteDTO(mapaId, unidadeNome, competencias, null);
    }

    /**
     * Salva ajustes no mapa (CDU-16).
     */
    @Transactional
    public SubprocessoDTO salvarAjustesMapa(Long subprocessoId, List<?> competencias, String usuarioTitulo) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        // Implementar lógica para salvar ajustes
        return SubprocessoMapper.toDTO(sp);
    }

    /**
     * Submete mapa ajustado (CDU-16).
     */
    @Transactional
    public SubprocessoDTO submeterMapaAjustado(Long subprocessoId, String usuarioTitulo) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        sp.setSituacaoId("MAPA_AJUSTADO");
        subprocessoRepository.save(sp);

        // Registrar movimentação
        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade());
        mov.setUnidadeDestino(sp.getUnidade().getUnidadeSuperior());
        mov.setDescricao("Submissão do mapa ajustado");
        movimentacaoRepository.save(mov);

        return SubprocessoMapper.toDTO(sp);
    }

    // Metodos auxiliares de notificação

    private void notificarSugestoes(Subprocesso sp) {
        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        if (unidadeSuperior != null) {
            // Email
            try {
                if (notificationService != null) {
                    notificationService.enviarEmail(
                            unidadeSuperior.getSigla(),
                            "SGC: Sugestões apresentadas para o mapa de competências da " + sp.getUnidade().getSigla(),
                            "A unidade " + sp.getUnidade().getSigla() + " apresentou sugestões para o mapa de competências elaborado no processo " + sp.getProcesso().getDescricao() + ". A análise dessas sugestões já pode ser realizada no sistema."
                    );
                }
            } catch (Exception ignored) {
            }

            // Alerta - simplificado
            if (alertaRepository != null) {
                Alerta alerta = new Alerta();
                alerta.setDescricao("Sugestões para o mapa de competências da " + sp.getUnidade().getSigla() + " aguardando análise");
                alerta.setProcesso(sp.getProcesso());
                alerta.setDataHora(java.time.LocalDateTime.now());
                alerta.setUnidadeOrigem(sp.getUnidade());
                alerta.setUnidadeDestino(unidadeSuperior);
                alertaRepository.save(alerta);
            }
        }
    }

    private void notificarValidacao(Subprocesso sp) {
        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        if (unidadeSuperior != null) {
            // Email
            try {
                if (notificationService != null) {
                    notificationService.enviarEmail(
                            unidadeSuperior.getSigla(),
                            "SGC: Validação do mapa de competências da " + sp.getUnidade().getSigla() + " submetida para análise",
                            "A unidade " + sp.getUnidade().getSigla() + " validou o mapa de competências elaborado no processo " + sp.getProcesso().getDescricao() + ". A análise dessa validação já pode ser realizada no sistema."
                    );
                }
            } catch (Exception ignored) {
            }

            // Alerta
            if (alertaRepository != null) {
                Alerta alerta = new Alerta();
                alerta.setDescricao("Validação do mapa de competências da " + sp.getUnidade().getSigla() + " aguardando análise");
                alerta.setProcesso(sp.getProcesso());
                alerta.setDataHora(java.time.LocalDateTime.now());
                alerta.setUnidadeOrigem(sp.getUnidade());
                alerta.setUnidadeDestino(unidadeSuperior);
                alertaRepository.save(alerta);
            }
        }
    }

    private void notificarDevolucao(Subprocesso sp, Unidade unidadeDevolucao) {
        // Email
        try {
            if (notificationService != null) {
                notificationService.enviarEmail(
                        unidadeDevolucao.getSigla(),
                        "SGC: Validação do mapa de competências da " + sp.getUnidade().getSigla() + " devolvida para ajustes",
                        "A validação do mapa de competências da " + sp.getUnidade().getSigla() + " no processo " + sp.getProcesso().getDescricao() + " foi devolvida para ajustes. Acompanhe o processo no sistema."
                );
            }
        } catch (Exception ignored) {
        }

        // Alerta
        if (alertaRepository != null) {
            Alerta alerta = new Alerta();
            alerta.setDescricao("Cadastro de atividades e conhecimentos da unidade " + sp.getUnidade().getSigla() + " devolvido para ajustes");
            alerta.setProcesso(sp.getProcesso());
            alerta.setDataHora(java.time.LocalDateTime.now());
            alerta.setUnidadeOrigem(sp.getUnidade().getUnidadeSuperior());
            alerta.setUnidadeDestino(unidadeDevolucao);
            alertaRepository.save(alerta);
        }
    }

    private void notificarAceite(Subprocesso sp) {
        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior().getUnidadeSuperior();
        if (unidadeSuperior != null) {
            // Email
            try {
                if (notificationService != null) {
                    notificationService.enviarEmail(
                            unidadeSuperior.getSigla(),
                            "SGC: Validação do mapa de competências da " + sp.getUnidade().getSigla() + " submetida para análise",
                            "A validação do mapa de competências da " + sp.getUnidade().getSigla() + " no processo " + sp.getProcesso().getDescricao() + " foi submetida para análise por essa unidade. A análise já pode ser realizada no sistema."
                    );
                }
            } catch (Exception ignored) {
            }

            // Alerta
            if (alertaRepository != null) {
                Alerta alerta = new Alerta();
                alerta.setDescricao("Validação do mapa de competências da " + sp.getUnidade().getSigla() + " submetida para análise");
                alerta.setProcesso(sp.getProcesso());
                alerta.setDataHora(java.time.LocalDateTime.now());
                alerta.setUnidadeOrigem(sp.getUnidade().getUnidadeSuperior());
                alerta.setUnidadeDestino(unidadeSuperior);
                alertaRepository.save(alerta);
            }
        }
    }

    private AnaliseValidacaoDTO mapearParaDTO(AnaliseValidacao analise) {
        return new AnaliseValidacaoDTO(
                analise.getCodigo(),
                analise.getDataHora(),
                analise.getObservacoes()
        );
    }

    // Metodos para cadastro (CDU-13 e CDU-14) - placeholders, implementar conforme necessário

    @Transactional
    public SubprocessoDTO devolverCadastro(Long subprocessoId, String motivo, String observacoes, String usuario) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        // Registrar análise
        AnaliseCadastro analise = new AnaliseCadastro();
        analise.setSubprocesso(sp);
        analise.setDataHora(java.time.LocalDateTime.now());
        analise.setObservacoes(motivo + (observacoes != null && !observacoes.isEmpty() ? " - " + observacoes : ""));
        analiseCadastroRepository.save(analise);

        // Identificar unidade de devolução (unidade do subprocesso)
        Unidade unidadeDevolucao = sp.getUnidade();

        // Registrar movimentação
        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade().getUnidadeSuperior()); // Assumindo que a devolução vem da unidade superior
        mov.setUnidadeDestino(unidadeDevolucao);
        mov.setDescricao("Devolução do cadastro de atividades para ajustes: " + motivo);
        movimentacaoRepository.save(mov);

        // Atualizar situação
        sp.setSituacaoId("CADASTRO_EM_ELABORACAO"); // Retorna para o estado inicial de elaboração
        sp.setDataFimEtapa1(null); // Limpa a data de fim da etapa 1, pois foi devolvido
        subprocessoRepository.save(sp);

        // Notificação e alerta (similar a notificarDevolucao)
        notificarDevolucaoCadastro(sp, unidadeDevolucao, motivo);

        return SubprocessoMapper.toDTO(sp);
    }

    private void notificarDevolucaoCadastro(Subprocesso sp, Unidade unidadeDevolucao, String motivo) {
        // Email
        try {
            if (notificationService != null) {
                notificationService.enviarEmail(
                        unidadeDevolucao.getSigla(),
                        "SGC: Cadastro de atividades da " + sp.getUnidade().getSigla() + " devolvido para ajustes",
                        "O cadastro de atividades da " + sp.getUnidade().getSigla() + " no processo " + sp.getProcesso().getDescricao() + " foi devolvido para ajustes com o motivo: " + motivo + ". Acompanhe o processo no sistema."
                );
            }
        } catch (Exception ignored) {
        }

        // Alerta
        if (alertaRepository != null) {
            Alerta alerta = new Alerta();
            alerta.setDescricao("Cadastro de atividades da unidade " + sp.getUnidade().getSigla() + " devolvido para ajustes: " + motivo);
            alerta.setProcesso(sp.getProcesso());
            alerta.setDataHora(java.time.LocalDateTime.now());
            alerta.setUnidadeOrigem(sp.getUnidade().getUnidadeSuperior());
            alerta.setUnidadeDestino(unidadeDevolucao);
            alertaRepository.save(alerta);
        }
    }

    @Transactional
    public SubprocessoDTO aceitarCadastro(Long subprocessoId, String observacoes, String usuario) {
        // Implementar lógica
        return null;
    }

    @Transactional
    public SubprocessoDTO homologarCadastro(Long subprocessoId, String observacoes, String usuario) {
        // Implementar lógica
        return null;
    }

    @Transactional
    public SubprocessoDTO devolverRevisaoCadastro(Long subprocessoId, String motivo, String observacoes, String usuario) {
        // Implementar lógica
        return null;
    }

    @Transactional
    public SubprocessoDTO aceitarRevisaoCadastro(Long subprocessoId, String observacoes, String usuario) {
        // Implementar lógica
        return null;
    }

    @Transactional
    public SubprocessoDTO homologarRevisaoCadastro(Long subprocessoId, String observacoes, String usuario) {
        // Implementar lógica
        return null;
    }
}
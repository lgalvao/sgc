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
import sgc.atividade.AtividadeMapper;
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
    private final AtividadeMapper atividadeMapper;
    private final ConhecimentoMapper conhecimentoMapper;
    private final MovimentacaoMapper movimentacaoMapper;
    private final SubprocessoMapper subprocessoMapper;

    @Transactional(readOnly = true)
    public SubprocessoDetalheDTO obterDetalhes(Long id, String perfil, Long unidadeUsuario) {
        if (perfil == null) {
            throw new ErroDominioAccessoNegado("Perfil inválido para acesso aos detalhes do subprocesso.");
        }

        Subprocesso sp = subprocessoRepository.findById(id)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(id)));

        if ("GESTOR".equalsIgnoreCase(perfil)) {
            if (sp.getUnidade() == null || unidadeUsuario == null || !unidadeUsuario.equals(sp.getUnidade().getCodigo())) {
                throw new ErroDominioAccessoNegado("Usuário sem permissão para visualizar este subprocesso.");
            }
        } else if (!"ADMIN".equalsIgnoreCase(perfil) && !"GESTOR".equalsIgnoreCase(perfil)) {
            throw new ErroDominioAccessoNegado("Perfil sem permissão.");
        }

        List<Movimentacao> movimentacoes = movimentacaoRepository.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
        final List<Atividade> atividades = (sp.getMapa() != null && sp.getMapa().getCodigo() != null)
                ? atividadeRepository.findByMapaCodigo(sp.getMapa().getCodigo())
                : emptyList();
        final Set<Long> atividadeIds = atividades.stream().map(Atividade::getCodigo).filter(Objects::nonNull).collect(Collectors.toSet());
        List<Conhecimento> conhecimentos = conhecimentoRepository.findAll().stream()
                .filter(c -> c.getAtividade() != null && atividadeIds.contains(c.getAtividade().getCodigo()))
                .collect(Collectors.toList());

        return toDetailDTO(sp, movimentacoes, atividades, conhecimentos);
    }

    private SubprocessoDetalheDTO toDetailDTO(Subprocesso sp, List<Movimentacao> movimentacoes, List<Atividade> atividades, List<Conhecimento> conhecimentos) {
        SubprocessoDetalheDTO dto = new SubprocessoDetalheDTO();

        if (sp.getUnidade() != null) {
            dto.setUnidade(new SubprocessoDetalheDTO.UnidadeDTO(sp.getUnidade().getCodigo(), sp.getUnidade().getSigla(), sp.getUnidade().getNome()));
        }

        if (sp.getUnidade() != null && sp.getUnidade().getTitular() != null) {
            var titular = sp.getUnidade().getTitular();
            dto.setResponsavel(new SubprocessoDetalheDTO.ResponsavelDTO(null, titular.getNome(), null, titular.getRamal(), titular.getEmail()));
        }

        dto.setSituacao(sp.getSituacaoId());

        if (movimentacoes != null && !movimentacoes.isEmpty()) {
            Movimentacao m = movimentacoes.getFirst();
            if (m.getUnidadeDestino() != null) {
                dto.setLocalizacaoAtual(m.getUnidadeDestino().getSigla());
            }
        }

        dto.setPrazoEtapaAtual(sp.getDataLimiteEtapa1() != null ? sp.getDataLimiteEtapa1() : sp.getDataLimiteEtapa2());

        dto.setMovimentacoes(movimentacoes.stream().map(movimentacaoMapper::toDTO).collect(Collectors.toList()));

        List<SubprocessoDetalheDTO.ElementoProcessoDTO> elementos = new ArrayList<>();
        if (atividades != null) {
            atividades.forEach(a -> elementos.add(new SubprocessoDetalheDTO.ElementoProcessoDTO("ATIVIDADE", atividadeMapper.toDTO(a))));
        }
        if (conhecimentos != null) {
            conhecimentos.forEach(c -> elementos.add(new SubprocessoDetalheDTO.ElementoProcessoDTO("CONHECIMENTO", conhecimentoMapper.toDTO(c))));
        }
        dto.setElementosDoProcesso(elementos);

        return dto;
    }

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
                        : ks.stream().map(conhecimentoMapper::toDTO).toList();

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

    @Transactional
    public void disponibilizarCadastroAcao(Long subprocessoId) {
        log.info("Iniciando disponibilização do cadastro para subprocesso {}", subprocessoId);
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        if (sp.getMapa() == null || sp.getMapa().getCodigo() == null) {
            log.warn("Validação falhou: subprocesso {} não possui mapa associado", subprocessoId);
            throw new IllegalStateException("Subprocesso sem mapa associado");
        }

        sp.setSituacaoId("CADASTRO_DISPONIBILIZADO");
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
        mov.setDescricao("Disponibilização do cadastro de atividades");
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
            n.setConteudo("Cadastro de atividades e conhecimentos da unidade " + (sp.getUnidade() != null ? sp.getUnidade().getSigla() : "") + " disponibilizado para análise");
            notificacaoRepository.save(n);
        }

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

    @Transactional
    public SubprocessoDTO disponibilizarMapa(Long subprocessoId, String observacoes, java.time.LocalDate dataLimiteEtapa2, String usuarioTitulo) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        if (sp.getMapa() == null) {
            throw new IllegalStateException("Subprocesso sem mapa associado");
        }

        sp.setSituacaoId("MAPA_DISPONIBILIZADO");
        sp.setDataLimiteEtapa2(dataLimiteEtapa2);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        subprocessoRepository.save(sp);

        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade());
        mov.setUnidadeDestino(sp.getUnidade());
        mov.setDescricao("Disponibilização do mapa de competências para validação");
        movimentacaoRepository.save(mov);

        notificarDisponibilizacaoMapa(sp);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDTO apresentarSugestoes(Long subprocessoId, String sugestoes, String usuarioTitulo) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        if (sp.getMapa() != null) {
            sp.getMapa().setSugestoes(sugestoes);
        }
        sp.setSituacaoId("MAPA_COM_SUGESTOES");
        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        subprocessoRepository.save(sp);

        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade());
        mov.setUnidadeDestino(sp.getUnidade().getUnidadeSuperior());
        mov.setDescricao("Apresentação de sugestões para o mapa de competências");
        movimentacaoRepository.save(mov);

        if (analiseValidacaoRepository != null) {
            analiseValidacaoRepository.deleteBySubprocesso_Codigo(sp.getCodigo());
        }

        notificarSugestoes(sp);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDTO validarMapa(Long subprocessoId, String usuarioTitulo) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        sp.setSituacaoId("MAPA_VALIDADO");
        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        subprocessoRepository.save(sp);

        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade());
        mov.setUnidadeDestino(sp.getUnidade().getUnidadeSuperior());
        mov.setDescricao("Validação do mapa de competências");
        movimentacaoRepository.save(mov);

        if (analiseValidacaoRepository != null) {
            analiseValidacaoRepository.deleteBySubprocesso_Codigo(sp.getCodigo());
        }

        notificarValidacao(sp);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional(readOnly = true)
    public SugestoesDTO obterSugestoes(Long subprocessoId) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        String sugestoes = sp.getMapa() != null ? sp.getMapa().getSugestoes() : null;
        boolean sugestoesApresentadas = sugestoes != null && !sugestoes.trim().isEmpty();
        String unidadeNome = sp.getUnidade() != null ? sp.getUnidade().getNome() : null;
        return new SugestoesDTO(sugestoes, sugestoesApresentadas, unidadeNome);
    }

    @Transactional(readOnly = true)
    public List<AnaliseValidacaoDTO> obterHistoricoValidacao(Long subprocessoId) {
        return analiseValidacaoRepository.findBySubprocesso_CodigoOrderByDataHoraDesc(subprocessoId)
                .stream()
                .map(this::mapearParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubprocessoDTO devolverValidacao(Long subprocessoId, String justificativa, String usuarioTitulo) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        AnaliseValidacao analise = new AnaliseValidacao();
        analise.setSubprocesso(sp);
        analise.setDataHora(java.time.LocalDateTime.now());
        analise.setObservacoes(justificativa);
        analiseValidacaoRepository.save(analise);

        Unidade unidadeDevolucao = sp.getUnidade();

        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade().getUnidadeSuperior());
        mov.setUnidadeDestino(unidadeDevolucao);
        mov.setDescricao("Devolução da validação do mapa de competências para ajustes");
        movimentacaoRepository.save(mov);

        sp.setSituacaoId("MAPA_DISPONIBILIZADO");
        sp.setDataFimEtapa2(null);
        subprocessoRepository.save(sp);

        notificarDevolucao(sp, unidadeDevolucao);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDTO aceitarValidacao(Long subprocessoId, String usuarioTitulo) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        AnaliseValidacao analise = new AnaliseValidacao();
        analise.setSubprocesso(sp);
        analise.setDataHora(java.time.LocalDateTime.now());
        analise.setObservacoes("Aceite da validação");
        analiseValidacaoRepository.save(analise);

        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade().getUnidadeSuperior());
        mov.setUnidadeDestino(sp.getUnidade().getUnidadeSuperior().getUnidadeSuperior());
        mov.setDescricao("Mapa de competências validado");
        movimentacaoRepository.save(mov);

        notificarAceite(sp);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDTO homologarValidacao(Long subprocessoId, String usuarioTitulo) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        sp.setSituacaoId("MAPA_HOMOLOGADO");
        subprocessoRepository.save(sp);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional(readOnly = true)
    public MapaAjusteDTO obterMapaParaAjuste(Long subprocessoId) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + subprocessoId));

        if (sp.getMapa() == null) {
            throw new IllegalStateException("Subprocesso sem mapa associado.");
        }

        Long mapaId = sp.getMapa().getCodigo();
        String unidadeNome = sp.getUnidade() != null ? sp.getUnidade().getNome() : "";
        
        String justificativa = "Ajustes necessários conforme análise anterior.";

        return new MapaAjusteDTO(mapaId, unidadeNome, new ArrayList<>(), justificativa);
    }

    @Transactional
    public SubprocessoDTO salvarAjustesMapa(Long subprocessoId, List<?> competencias, String usuarioTitulo) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + subprocessoId));

        if (!"REVISAO_CADASTRO_HOMOLOGADA".equals(sp.getSituacaoId())) {
            throw new IllegalStateException("Ajustes no mapa só podem ser feitos quando a revisão do cadastro está homologada.");
        }

        log.info("Salvando ajustes para o mapa do subprocesso {}...", subprocessoId);

        sp.setSituacaoId("MAPA_AJUSTADO");
        subprocessoRepository.save(sp);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDTO submeterMapaAjustado(Long subprocessoId, String usuarioTitulo) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        sp.setSituacaoId("MAPA_AJUSTADO");
        subprocessoRepository.save(sp);

        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade());
        mov.setUnidadeDestino(sp.getUnidade().getUnidadeSuperior());
        mov.setDescricao("Submissão do mapa ajustado");
        movimentacaoRepository.save(mov);

        return subprocessoMapper.toDTO(sp);
    }

    private void notificarDisponibilizacaoMapa(Subprocesso sp) {
        Unidade unidade = sp.getUnidade();
        if (unidade == null) return;

        String emailSubject = "SGC: Mapa de competências disponibilizado para validação";
        String emailBody = String.format(
            "O mapa de competências da unidade %s foi disponibilizado para validação no processo '%s'. O prazo para conclusão desta etapa é %s.",
            unidade.getSigla(),
            sp.getProcesso().getDescricao(),
            sp.getDataLimiteEtapa2()
        );

        try {
            notificationService.enviarEmail(unidade.getSigla(), emailSubject, emailBody);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de disponibilização de mapa para unidade {}: {}", unidade.getSigla(), e.getMessage());
        }

        Unidade superior = unidade.getUnidadeSuperior();
        while (superior != null) {
            try {
                notificationService.enviarEmail(superior.getSigla(), emailSubject, emailBody);
            } catch (Exception e) {
                log.error("Falha ao enviar e-mail de disponibilização de mapa para unidade superior {}: {}", superior.getSigla(), e.getMessage());
            }
            superior = superior.getUnidadeSuperior();
        }

        Alerta alerta = new Alerta();
        alerta.setDescricao("Mapa de competências da unidade " + unidade.getSigla() + " disponibilizado para análise");
        alerta.setProcesso(sp.getProcesso());
        alerta.setDataHora(java.time.LocalDateTime.now());
        alerta.setUnidadeOrigem(null);
        alerta.setUnidadeDestino(unidade);
        alertaRepository.save(alerta);
    }

    private void notificarSugestoes(Subprocesso sp) {
        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        if (unidadeSuperior != null) {
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

    @Transactional
    public SubprocessoDTO devolverCadastro(Long subprocessoId, String motivo, String observacoes, String usuario) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(subprocessoId)));

        AnaliseCadastro analise = new AnaliseCadastro();
        analise.setSubprocesso(sp);
        analise.setDataHora(java.time.LocalDateTime.now());
        analise.setObservacoes(motivo + (observacoes != null && !observacoes.isEmpty() ? " - " + observacoes : ""));
        analiseCadastroRepository.save(analise);

        Unidade unidadeDevolucao = sp.getUnidade();

        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade().getUnidadeSuperior());
        mov.setUnidadeDestino(unidadeDevolucao);
        mov.setDescricao("Devolução do cadastro de atividades para ajustes: " + motivo);
        movimentacaoRepository.save(mov);

        sp.setSituacaoId("CADASTRO_EM_ELABORACAO");
        sp.setDataFimEtapa1(null);
        subprocessoRepository.save(sp);

        notificarDevolucaoCadastro(sp, unidadeDevolucao, motivo);

        return subprocessoMapper.toDTO(sp);
    }

    private void notificarDevolucaoCadastro(Subprocesso sp, Unidade unidadeDevolucao, String motivo) {
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
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + subprocessoId));

        if (!"CADASTRO_DISPONIBILIZADO".equals(sp.getSituacaoId())) {
            throw new IllegalStateException("Ação de aceite só pode ser executada em cadastros disponibilizados.");
        }

        AnaliseCadastro analise = new AnaliseCadastro();
        analise.setSubprocesso(sp);
        analise.setDataHora(java.time.LocalDateTime.now());
        analise.setAcao("ACEITE");
        analise.setAnalistaUsuarioTitulo(usuario);
        analise.setObservacoes(observacoes);
        analiseCadastroRepository.save(analise);

        Unidade unidadeOrigem = sp.getUnidade();
        Unidade unidadeDestino = unidadeOrigem.getUnidadeSuperior();
        if (unidadeDestino == null) {
            throw new IllegalStateException("Não foi possível identificar a unidade superior para enviar a análise.");
        }

        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(unidadeOrigem);
        mov.setUnidadeDestino(unidadeDestino);
        mov.setDescricao("Cadastro de atividades e conhecimentos aceito");
        movimentacaoRepository.save(mov);

        log.info("Notificando unidade {} sobre aceite do cadastro da unidade {}", unidadeDestino.getSigla(), unidadeOrigem.getSigla());


        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDTO homologarCadastro(Long subprocessoId, String observacoes, String usuario) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + subprocessoId));

        if (!"CADASTRO_DISPONIBILIZADO".equals(sp.getSituacaoId())) {
            throw new IllegalStateException("Ação de homologar só pode ser executada em cadastros disponibilizados.");
        }

        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade().getUnidadeSuperior()); 
        mov.setUnidadeDestino(sp.getUnidade().getUnidadeSuperior());
        mov.setDescricao("Cadastro de atividades e conhecimentos homologado");
        movimentacaoRepository.save(mov);

        sp.setSituacaoId("CADASTRO_HOMOLOGADO");
        subprocessoRepository.save(sp);
        
        log.info("Subprocesso {} homologado com sucesso.", subprocessoId);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDTO devolverRevisaoCadastro(Long subprocessoId, String motivo, String observacoes, String usuario) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + subprocessoId));

        if (!"REVISAO_CADASTRO_DISPONIBILIZADA".equals(sp.getSituacaoId())) {
            throw new IllegalStateException("Ação de devolução só pode ser executada em revisões de cadastro disponibilizadas.");
        }

        AnaliseCadastro analise = new AnaliseCadastro();
        analise.setSubprocesso(sp);
        analise.setDataHora(java.time.LocalDateTime.now());
        analise.setAcao("DEVOLUCAO_REVISAO");
        analise.setMotivo(motivo);
        analise.setObservacoes(observacoes);
        analise.setAnalistaUsuarioTitulo(usuario);
        analiseCadastroRepository.save(analise);

        Unidade unidadeDevolucao = sp.getUnidade();

        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(unidadeDevolucao.getUnidadeSuperior());
        mov.setUnidadeDestino(unidadeDevolucao);
        mov.setDescricao("Devolução da revisão do cadastro para ajustes: " + motivo);
        movimentacaoRepository.save(mov);

        sp.setSituacaoId("REVISAO_CADASTRO_EM_ANDAMENTO");
        sp.setDataFimEtapa1(null);
        subprocessoRepository.save(sp);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDTO aceitarRevisaoCadastro(Long subprocessoId, String observacoes, String usuario) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + subprocessoId));

        if (!"REVISAO_CADASTRO_DISPONIBILIZADA".equals(sp.getSituacaoId())) {
            throw new IllegalStateException("Ação de aceite só pode ser executada em revisões de cadastro disponibilizadas.");
        }

        AnaliseCadastro analise = new AnaliseCadastro();
        analise.setSubprocesso(sp);
        analise.setDataHora(java.time.LocalDateTime.now());
        analise.setAcao("ACEITE_REVISAO");
        analise.setObservacoes(observacoes);
        analise.setAnalistaUsuarioTitulo(usuario);
        analiseCadastroRepository.save(analise);

        Unidade unidadeOrigem = sp.getUnidade();
        Unidade unidadeDestino = unidadeOrigem.getUnidadeSuperior();

        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(unidadeOrigem);
        mov.setUnidadeDestino(unidadeDestino);
        mov.setDescricao("Revisão do cadastro de atividades e conhecimentos aceita");
        movimentacaoRepository.save(mov);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDTO homologarRevisaoCadastro(Long subprocessoId, String observacoes, String usuario) {
        Subprocesso sp = subprocessoRepository.findById(subprocessoId)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + subprocessoId));

        if (!"REVISAO_CADASTRO_DISPONIBILIZADA".equals(sp.getSituacaoId())) {
            throw new IllegalStateException("Ação de homologar só pode ser executada em revisões de cadastro disponibilizadas.");
        }

        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sp.getUnidade().getUnidadeSuperior());
        mov.setUnidadeDestino(sp.getUnidade().getUnidadeSuperior());
        mov.setDescricao("Revisão do cadastro de atividades e conhecimentos homologada");
        movimentacaoRepository.save(mov);

        sp.setSituacaoId("REVISAO_CADASTRO_HOMOLOGADA");
        subprocessoRepository.save(sp);

        return subprocessoMapper.toDTO(sp);
    }
}

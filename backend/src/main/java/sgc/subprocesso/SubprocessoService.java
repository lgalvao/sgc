package sgc.subprocesso;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.analise.modelo.AnaliseCadastro;
import sgc.analise.modelo.AnaliseCadastroRepo;
import sgc.analise.enums.TipoAcaoAnalise;
import sgc.analise.modelo.AnaliseValidacao;
import sgc.analise.modelo.AnaliseValidacaoRepo;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.enums.SituacaoSubprocesso;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.conhecimento.dto.ConhecimentoDto;
import sgc.conhecimento.dto.ConhecimentoMapper;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.notificacao.NotificacaoService;
import sgc.notificacao.modelo.Notificacao;
import sgc.notificacao.modelo.NotificacaoRepo;
import sgc.processo.eventos.SubprocessoDisponibilizadoEvento;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;

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
    private final SubprocessoRepo repositorioSubprocesso;
    private final MovimentacaoRepo repositorioMovimentacao;
    private final AtividadeRepo atividadeRepo;
    private final ConhecimentoRepo repositorioConhecimento;
    private final AnaliseCadastroRepo repositorioAnaliseCadastro;
    private final AnaliseValidacaoRepo repositorioAnaliseValidacao;
    private final NotificacaoRepo repositorioNotificacao;
    private final CompetenciaRepo competenciaRepo;
    private final CompetenciaAtividadeRepo competenciaAtividadeRepo;
    private final NotificacaoService notificacaoService;
    private final ApplicationEventPublisher publicadorDeEventos;
    private final AlertaRepo repositorioAlerta;
    private final AtividadeMapper atividadeMapper;
    private final ConhecimentoMapper conhecimentoMapper;
    private final MovimentacaoMapper movimentacaoMapper;
    private final SubprocessoMapper subprocessoMapper;

    @Transactional(readOnly = true)
    public SubprocessoDetalheDto obterDetalhes(Long id, String perfil, Long unidadeUsuario) {
        if (perfil == null) {
            throw new ErroDominioAccessoNegado("Perfil inválido para acesso aos detalhes do subprocesso.");
        }

        Subprocesso sp = repositorioSubprocesso.findById(id)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(id)));

        if ("GESTOR".equalsIgnoreCase(perfil)) {
            if (sp.getUnidade() == null || unidadeUsuario == null || !unidadeUsuario.equals(sp.getUnidade().getCodigo())) {
                throw new ErroDominioAccessoNegado("Usuário sem permissão para visualizar este subprocesso.");
            }
        } else if (!"ADMIN".equalsIgnoreCase(perfil) && !"GESTOR".equalsIgnoreCase(perfil)) {
            throw new ErroDominioAccessoNegado("Perfil sem permissão.");
        }

        List<Movimentacao> movimentacoes = repositorioMovimentacao.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
        final List<Atividade> atividades = (sp.getMapa() != null && sp.getMapa().getCodigo() != null)
                ? atividadeRepo.findByMapaCodigo(sp.getMapa().getCodigo())
                : emptyList();
        final Set<Long> idsAtividades = atividades.stream().map(Atividade::getCodigo).filter(Objects::nonNull).collect(Collectors.toSet());
        List<Conhecimento> conhecimentos = repositorioConhecimento.findAll().stream()
                .filter(c -> c.getAtividade() != null && idsAtividades.contains(c.getAtividade().getCodigo()))
                .collect(Collectors.toList());

        return paraDetalheDTO(sp, movimentacoes, atividades, conhecimentos);
    }

    private SubprocessoDetalheDto paraDetalheDTO(Subprocesso sp, List<Movimentacao> movimentacoes, List<Atividade> atividades, List<Conhecimento> conhecimentos) {
        SubprocessoDetalheDto.UnidadeDTO unidadeDto = null;
        if (sp.getUnidade() != null) {
            unidadeDto = new SubprocessoDetalheDto.UnidadeDTO(sp.getUnidade().getCodigo(), sp.getUnidade().getSigla(), sp.getUnidade().getNome());
        }

        SubprocessoDetalheDto.ResponsavelDTO responsavelDto = null;
        if (sp.getUnidade() != null && sp.getUnidade().getTitular() != null) {
            var titular = sp.getUnidade().getTitular();
            responsavelDto = new SubprocessoDetalheDto.ResponsavelDTO(null, titular.getNome(), null, titular.getRamal(), titular.getEmail());
        }

        String localizacaoAtual = null;
        if (movimentacoes != null && !movimentacoes.isEmpty()) {
            Movimentacao m = movimentacoes.getFirst();
            if (m.getUnidadeDestino() != null) {
                localizacaoAtual = m.getUnidadeDestino().getSigla();
            }
        }

        var prazoEtapaAtual = sp.getDataLimiteEtapa1() != null ? sp.getDataLimiteEtapa1() : sp.getDataLimiteEtapa2();

        List<MovimentacaoDto> movimentacoesDto = new ArrayList<>();
        if (movimentacoes != null) {
            movimentacoesDto = movimentacoes.stream().map(movimentacaoMapper::toDTO).collect(Collectors.toList());
        }

        List<SubprocessoDetalheDto.ElementoProcessoDTO> elementos = new ArrayList<>();
        if (atividades != null) {
            atividades.forEach(a -> elementos.add(new SubprocessoDetalheDto.ElementoProcessoDTO("ATIVIDADE", atividadeMapper.toDTO(a))));
        }
        if (conhecimentos != null) {
            conhecimentos.forEach(c -> elementos.add(new SubprocessoDetalheDto.ElementoProcessoDTO("CONHECIMENTO", conhecimentoMapper.toDTO(c))));
        }

        return new SubprocessoDetalheDto(
            unidadeDto,
            responsavelDto,
            sp.getSituacao().name(),
            localizacaoAtual,
            prazoEtapaAtual,
            movimentacoesDto,
            elementos
        );
    }

    @Transactional(readOnly = true)
    public SubprocessoCadastroDto obterCadastro(Long idSubprocesso) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        List<SubprocessoCadastroDto.AtividadeCadastroDTO> atividadesComConhecimentos = new ArrayList<>();
        if (sp.getMapa() != null && sp.getMapa().getCodigo() != null) {
            List<Atividade> atividades = atividadeRepo.findByMapaCodigo(sp.getMapa().getCodigo());
            if (atividades == null) atividades = emptyList();

            for (Atividade a : atividades) {
                List<Conhecimento> ks = repositorioConhecimento.findByAtividadeCodigo(a.getCodigo());
                List<ConhecimentoDto> ksDto = ks == null
                        ? emptyList()
                        : ks.stream().map(conhecimentoMapper::toDTO).toList();

                atividadesComConhecimentos.add(new SubprocessoCadastroDto.AtividadeCadastroDTO(
                        a.getCodigo(),
                        a.getDescricao(),
                        ksDto
                ));
            }
        }

        return new SubprocessoCadastroDto(
                sp.getCodigo(),
                sp.getUnidade() != null ? sp.getUnidade().getSigla() : null,
                atividadesComConhecimentos
        );
    }


    @Transactional(readOnly = true)
    public List<Atividade> obterAtividadesSemConhecimento(Long idSubprocesso) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        if (sp.getMapa() == null || sp.getMapa().getCodigo() == null) {
            return emptyList();
        }

        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(sp.getMapa().getCodigo());
        if (atividades == null || atividades.isEmpty()) {
            return emptyList();
        }

        return atividades.stream()
                .filter(a -> {
                    if (a.getCodigo() == null) return true;
                    List<Conhecimento> ks = repositorioConhecimento.findByAtividadeCodigo(a.getCodigo());
                    return ks == null || ks.isEmpty();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void disponibilizarCadastro(Long idSubprocesso) {
        log.info("Iniciando disponibilização do cadastro para subprocesso {}", idSubprocesso);
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        if (sp.getMapa() == null || sp.getMapa().getCodigo() == null) {
            log.warn("Validação falhou: subprocesso {} não possui mapa associado", idSubprocesso);
            throw new IllegalStateException("Subprocesso sem mapa associado");
        }

        sp.setSituacao(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        Unidade unidadeSuperior = sp.getUnidade() != null ? sp.getUnidade().getUnidadeSuperior() : null;

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade(), unidadeSuperior, "Disponibilização do cadastro de atividades"));

        repositorioAnaliseCadastro.deleteBySubprocessoCodigo(sp.getCodigo());

        Notificacao n = new Notificacao();
        n.setSubprocesso(sp);
        n.setDataHora(java.time.LocalDateTime.now());
        n.setUnidadeOrigem(sp.getUnidade());
        n.setUnidadeDestino(unidadeSuperior);
        n.setConteudo("Cadastro de atividades e conhecimentos da unidade " + (sp.getUnidade() != null ? sp.getUnidade().getSigla() : "") + " disponibilizado para análise");
        repositorioNotificacao.save(n);

        if (notificacaoService != null && unidadeSuperior != null) {
            notificacaoService.enviarEmail(unidadeSuperior.getSigla(), "Cadastro disponibilizado", "Cadastro de atividades disponibilizado para análise");
        }

        publicadorDeEventos.publishEvent(new SubprocessoDisponibilizadoEvento(sp.getCodigo()));
        log.info("Disponibilização do cadastro concluída com sucesso para subprocesso {}", idSubprocesso);
    }

    @Transactional
    public void disponibilizarRevisao(Long idSubprocesso) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        Unidade unidadeSuperior = sp.getUnidade() != null ? sp.getUnidade().getUnidadeSuperior() : null;

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade(), unidadeSuperior, "Disponibilização da revisão do cadastro de atividades"));
        repositorioAnaliseCadastro.deleteBySubprocessoCodigo(sp.getCodigo());

        Notificacao n = new Notificacao();
        n.setSubprocesso(sp);
        n.setDataHora(java.time.LocalDateTime.now());
        n.setUnidadeOrigem(sp.getUnidade());
        n.setUnidadeDestino(unidadeSuperior);
        n.setConteudo("Revisão do cadastro de atividades da unidade %s disponibilizada para análise".formatted(sp.getUnidade() != null ? sp.getUnidade().getSigla() : ""));
        repositorioNotificacao.save(n);

        if (notificacaoService != null && unidadeSuperior != null) {
            notificacaoService.enviarEmail(unidadeSuperior.getSigla(), "Revisão do cadastro disponibilizada", "Revisão do cadastro de atividades disponibilizada para análise");
        }
    }

    @Transactional
    public SubprocessoDto disponibilizarMapa(Long idSubprocesso, String observacoes, java.time.LocalDate dataLimiteEtapa2, String usuarioTitulo) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        if (sp.getMapa() == null) {
            throw new IllegalStateException("Subprocesso sem mapa associado");
        }

        sp.setSituacao(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
        sp.setDataLimiteEtapa2(dataLimiteEtapa2);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade(), sp.getUnidade(), "Disponibilização do mapa de competências para validação"));
        notificarDisponibilizacaoMapa(sp);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDto apresentarSugestoes(Long idSubprocesso, String sugestoes, String usuarioTitulo) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        if (sp.getMapa() != null) {
            sp.getMapa().setSugestoes(sugestoes);
        }
        sp.setSituacao(SituacaoSubprocesso.MAPA_COM_SUGESTOES);
        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade(), sp.getUnidade().getUnidadeSuperior(), "Apresentação de sugestões para o mapa de competências"));
        repositorioAnaliseValidacao.deleteBySubprocesso_Codigo(sp.getCodigo());
        notificarSugestoes(sp);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDto validarMapa(Long idSubprocesso, String usuarioTitulo) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        sp.setSituacao(SituacaoSubprocesso.MAPA_VALIDADO);
        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade(), sp.getUnidade().getUnidadeSuperior(), "Validação do mapa de competências"));
        repositorioAnaliseValidacao.deleteBySubprocesso_Codigo(sp.getCodigo());
        notificarValidacao(sp);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional(readOnly = true)
    public SugestoesDto obterSugestoes(Long idSubprocesso) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        String sugestoes = sp.getMapa() != null ? sp.getMapa().getSugestoes() : null;
        boolean sugestoesApresentadas = sugestoes != null && !sugestoes.trim().isEmpty();
        String nomeUnidade = sp.getUnidade() != null ? sp.getUnidade().getNome() : null;
        return new SugestoesDto(sugestoes, sugestoesApresentadas, nomeUnidade);
    }

    @Transactional(readOnly = true)
    public List<AnaliseValidacaoDto> obterHistoricoValidacao(Long idSubprocesso) {
        return repositorioAnaliseValidacao.findBySubprocesso_CodigoOrderByDataHoraDesc(idSubprocesso)
                .stream()
                .map(this::mapearParaAnaliseValidacaoDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubprocessoDto devolverValidacao(Long idSubprocesso, String justificativa, String usuarioTitulo) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        AnaliseValidacao analise = new AnaliseValidacao();
        analise.setSubprocesso(sp);
        analise.setDataHora(java.time.LocalDateTime.now());
        analise.setObservacoes(justificativa);
        repositorioAnaliseValidacao.save(analise);

        Unidade unidadeDevolucao = sp.getUnidade();

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade().getUnidadeSuperior(), unidadeDevolucao, "Devolução da validação do mapa de competências para ajustes"));

        sp.setSituacao(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
        sp.setDataFimEtapa2(null);
        repositorioSubprocesso.save(sp);

        notificarDevolucao(sp, unidadeDevolucao);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDto aceitarValidacao(Long idSubprocesso, String usuarioTitulo) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        AnaliseValidacao analise = new AnaliseValidacao();
        analise.setSubprocesso(sp);
        analise.setDataHora(java.time.LocalDateTime.now());
        analise.setObservacoes("Aceite da validação");
        repositorioAnaliseValidacao.save(analise);

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade().getUnidadeSuperior(), sp.getUnidade().getUnidadeSuperior().getUnidadeSuperior(), "Mapa de competências validado"));
        notificarAceite(sp);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDto homologarValidacao(Long idSubprocesso, String usuarioTitulo) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        sp.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        repositorioSubprocesso.save(sp);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional(readOnly = true)
    public MapaAjusteDto obterMapaParaAjuste(Long idSubprocesso) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (sp.getMapa() == null) {
            throw new IllegalStateException("Subprocesso sem mapa associado.");
        }

        Long idMapa = sp.getMapa().getCodigo();
        String nomeUnidade = sp.getUnidade() != null ? sp.getUnidade().getNome() : "";

        String justificativa = repositorioAnaliseValidacao.findFirstBySubprocesso_CodigoOrderByDataHoraDesc(idSubprocesso)
                .map(AnaliseValidacao::getObservacoes)
                .orElse(null);

        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(idMapa);
        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(idMapa);
        List<CompetenciaAjusteDto> competenciaDtos = new ArrayList<>();

        for (Competencia comp : competencias) {
            List<AtividadeAjusteDto> atividadeDtos = new ArrayList<>();
            for (Atividade ativ : atividades) {
                List<Conhecimento> conhecimentos = repositorioConhecimento.findByAtividadeCodigo(ativ.getCodigo());
                boolean isLinked = competenciaAtividadeRepo.existsById(new CompetenciaAtividade.Id(comp.getCodigo(), ativ.getCodigo()));
                List<ConhecimentoAjusteDto> conhecimentoDtos = conhecimentos.stream()
                        .map(con -> new ConhecimentoAjusteDto(con.getCodigo(), con.getDescricao(), isLinked))
                        .collect(Collectors.toList());
                atividadeDtos.add(new AtividadeAjusteDto(ativ.getCodigo(), ativ.getDescricao(), conhecimentoDtos));
            }
            competenciaDtos.add(new CompetenciaAjusteDto(comp.getCodigo(), comp.getDescricao(), atividadeDtos));
        }

        return new MapaAjusteDto(idMapa, nomeUnidade, competenciaDtos, justificativa);
    }

    @Transactional
    public SubprocessoDto salvarAjustesMapa(Long idSubprocesso, List<CompetenciaAjusteDto> competencias, String usuarioTitulo) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA &&
            sp.getSituacao() != SituacaoSubprocesso.MAPA_DISPONIBILIZADO &&
            sp.getSituacao() != SituacaoSubprocesso.MAPA_AJUSTADO) {
             throw new IllegalStateException("Ajustes no mapa só podem ser feitos em estados específicos. Situação atual: " + sp.getSituacao());
        }

        log.info("Salvando ajustes para o mapa do subprocesso {}...", idSubprocesso);

        for (CompetenciaAjusteDto compDto : competencias) {
            List<CompetenciaAtividade> linksExistentes = competenciaAtividadeRepo.findByCompetenciaCodigo(compDto.competenciaId());
            if (linksExistentes != null && !linksExistentes.isEmpty()) {
                competenciaAtividadeRepo.deleteAll(linksExistentes);
            }
        }

        for (CompetenciaAjusteDto compDto : competencias) {
            for (AtividadeAjusteDto ativDto : compDto.atividades()) {
                boolean deveVincular = ativDto.conhecimentos().stream().anyMatch(ConhecimentoAjusteDto::incluido);
                if (deveVincular) {
                    CompetenciaAtividade novoLink = new CompetenciaAtividade();
                    novoLink.setId(new CompetenciaAtividade.Id(compDto.competenciaId(), ativDto.atividadeId()));
                    competenciaAtividadeRepo.save(novoLink);
                }
            }
        }

        sp.setSituacao(SituacaoSubprocesso.MAPA_AJUSTADO);
        repositorioSubprocesso.save(sp);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDto submeterMapaAjustado(Long idSubprocesso, String usuarioTitulo) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        sp.setSituacao(SituacaoSubprocesso.MAPA_AJUSTADO);
        repositorioSubprocesso.save(sp);

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade(), sp.getUnidade().getUnidadeSuperior(), "Submissão do mapa ajustado"));

        return subprocessoMapper.toDTO(sp);
    }

    private void notificarDisponibilizacaoMapa(Subprocesso sp) {
        Unidade unidade = sp.getUnidade();
        if (unidade == null) return;

        String nomeProcesso = sp.getProcesso().getDescricao();
        String siglaUnidade = unidade.getSigla();
        String dataLimite = sp.getDataLimiteEtapa2().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // Item 17: E-mail para a própria unidade
        String assuntoUnidade = "SGC: Mapa de Competências da sua unidade disponibilizado para validação";
        String corpoUnidade = String.format(
            "O mapa de competências da sua unidade (%s) foi disponibilizado para validação no processo '%s'. O prazo para conclusão desta etapa é %s.",
            siglaUnidade,
            nomeProcesso,
            dataLimite
        );
        notificacaoService.enviarEmail(unidade.getSigla(), assuntoUnidade, corpoUnidade);

        // Item 18: E-mail para as unidades superiores
        String assuntoSuperior = "SGC: Mapa de Competências da unidade " + siglaUnidade + " disponibilizado para validação";
        String corpoSuperior = String.format(
            "O mapa de competências da unidade %s foi disponibilizado para validação no processo '%s'. O prazo para conclusão desta etapa é %s. Acompanhe o processo no sistema.",
            siglaUnidade,
            nomeProcesso,
            dataLimite
        );
        Unidade superior = unidade.getUnidadeSuperior();
        while (superior != null) {
            notificacaoService.enviarEmail(superior.getSigla(), assuntoSuperior, corpoSuperior);
            superior = superior.getUnidadeSuperior();
        }

        // Item 16: Alerta para a própria unidade
        Alerta alerta = new Alerta();
        alerta.setDescricao(String.format("Seu mapa de competências está disponível para validação (Processo: %s)", nomeProcesso));
        alerta.setProcesso(sp.getProcesso());
        alerta.setDataHora(java.time.LocalDateTime.now());
        alerta.setUnidadeOrigem(null); // Origem é o sistema
        alerta.setUnidadeDestino(unidade);
        repositorioAlerta.save(alerta);
    }

    private void notificarSugestoes(Subprocesso sp) {
        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        if (unidadeSuperior != null) {
            notificacaoService.enviarEmail(
                unidadeSuperior.getSigla(),
                "SGC: Sugestões apresentadas para o mapa de competências da " + sp.getUnidade().getSigla(),
                "A unidade " + sp.getUnidade().getSigla() + " apresentou sugestões para o mapa de competências elaborado no processo " + sp.getProcesso().getDescricao() + ". A análise dessas sugestões já pode ser realizada no sistema."
            );

            Alerta alerta = new Alerta();
            alerta.setDescricao("Sugestões para o mapa de competências da " + sp.getUnidade().getSigla() + " aguardando análise");
            alerta.setProcesso(sp.getProcesso());
            alerta.setDataHora(java.time.LocalDateTime.now());
            alerta.setUnidadeOrigem(sp.getUnidade());
            alerta.setUnidadeDestino(unidadeSuperior);
            repositorioAlerta.save(alerta);
        }
    }

    private void notificarValidacao(Subprocesso sp) {
        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        if (unidadeSuperior != null) {
            notificacaoService.enviarEmail(
                unidadeSuperior.getSigla(),
                "SGC: Validação do mapa de competências da " + sp.getUnidade().getSigla() + " submetida para análise",
                "A unidade " + sp.getUnidade().getSigla() + " validou o mapa de competências elaborado no processo " + sp.getProcesso().getDescricao() + ". A análise dessa validação já pode ser realizada no sistema."
            );

            Alerta alerta = new Alerta();
            alerta.setDescricao("Validação do mapa de competências da " + sp.getUnidade().getSigla() + " aguardando análise");
            alerta.setProcesso(sp.getProcesso());
            alerta.setDataHora(java.time.LocalDateTime.now());
            alerta.setUnidadeOrigem(sp.getUnidade());
            alerta.setUnidadeDestino(unidadeSuperior);
            repositorioAlerta.save(alerta);
        }
    }

    private void notificarDevolucao(Subprocesso sp, Unidade unidadeDevolucao) {
        notificacaoService.enviarEmail(
            unidadeDevolucao.getSigla(),
            "SGC: Validação do mapa de competências da " + sp.getUnidade().getSigla() + " devolvida para ajustes",
            "A validação do mapa de competências da " + sp.getUnidade().getSigla() + " no processo " + sp.getProcesso().getDescricao() + " foi devolvida para ajustes. Acompanhe o processo no sistema."
        );

        Alerta alerta = new Alerta();
        alerta.setDescricao("Cadastro de atividades e conhecimentos da unidade " + sp.getUnidade().getSigla() + " devolvido para ajustes");
        alerta.setProcesso(sp.getProcesso());
        alerta.setDataHora(java.time.LocalDateTime.now());
        alerta.setUnidadeOrigem(sp.getUnidade().getUnidadeSuperior());
        alerta.setUnidadeDestino(unidadeDevolucao);
        repositorioAlerta.save(alerta);
    }

    private void notificarAceite(Subprocesso sp) {
        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior().getUnidadeSuperior();
        if (unidadeSuperior != null) {
            notificacaoService.enviarEmail(
                unidadeSuperior.getSigla(),
                "SGC: Validação do mapa de competências da " + sp.getUnidade().getSigla() + " submetida para análise",
                "A validação do mapa de competências da " + sp.getUnidade().getSigla() + " no processo " + sp.getProcesso().getDescricao() + " foi submetida para análise por essa unidade. A análise já pode ser realizada no sistema."
            );

            Alerta alerta = new Alerta();
            alerta.setDescricao("Validação do mapa de competências da " + sp.getUnidade().getSigla() + " submetida para análise");
            alerta.setProcesso(sp.getProcesso());
            alerta.setDataHora(java.time.LocalDateTime.now());
            alerta.setUnidadeOrigem(sp.getUnidade().getUnidadeSuperior());
            alerta.setUnidadeDestino(unidadeSuperior);
            repositorioAlerta.save(alerta);
        }
    }

    private AnaliseValidacaoDto mapearParaAnaliseValidacaoDTO(AnaliseValidacao analise) {
        return new AnaliseValidacaoDto(
                analise.getCodigo(),
                analise.getDataHora(),
                analise.getObservacoes()
        );
    }

    @Transactional
    public SubprocessoDto devolverCadastro(Long idSubprocesso, String motivo, String observacoes, String usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        AnaliseCadastro analise = new AnaliseCadastro();
        analise.setSubprocesso(sp);
        analise.setDataHora(java.time.LocalDateTime.now());
        analise.setObservacoes(motivo + (observacoes != null && !observacoes.isEmpty() ? " - " + observacoes : ""));
        repositorioAnaliseCadastro.save(analise);

        Unidade unidadeDevolucao = sp.getUnidade();

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade().getUnidadeSuperior(), unidadeDevolucao, "Devolução do cadastro de atividades para ajustes: " + motivo));
        sp.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
        sp.setDataFimEtapa1(null);
        repositorioSubprocesso.save(sp);

        notificarDevolucaoCadastro(sp, unidadeDevolucao, motivo);

        return subprocessoMapper.toDTO(sp);
    }

    private void notificarDevolucaoCadastro(Subprocesso sp, Unidade unidadeDevolucao, String motivo) {
        notificacaoService.enviarEmail(
            unidadeDevolucao.getSigla(),
            "SGC: Cadastro de atividades da " + sp.getUnidade().getSigla() + " devolvido para ajustes",
            "O cadastro de atividades da " + sp.getUnidade().getSigla() + " no processo " + sp.getProcesso().getDescricao() + " foi devolvido para ajustes com o motivo: " + motivo + ". Acompanhe o processo no sistema."
        );

        Alerta alerta = new Alerta();
        alerta.setDescricao("Cadastro de atividades da unidade " + sp.getUnidade().getSigla() + " devolvido para ajustes: " + motivo);
        alerta.setProcesso(sp.getProcesso());
        alerta.setDataHora(java.time.LocalDateTime.now());
        alerta.setUnidadeOrigem(sp.getUnidade().getUnidadeSuperior());
        alerta.setUnidadeDestino(unidadeDevolucao);
        repositorioAlerta.save(alerta);
    }

    @Transactional
    public SubprocessoDto aceitarCadastro(Long idSubprocesso, String observacoes, String usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (sp.getSituacao() != SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO) {
            throw new IllegalStateException("Ação de aceite só pode ser executada em cadastros disponibilizados.");
        }

        AnaliseCadastro analise = new AnaliseCadastro();
        analise.setSubprocesso(sp);
        analise.setDataHora(java.time.LocalDateTime.now());
        analise.setAcao(TipoAcaoAnalise.ACEITE);
        analise.setAnalistaUsuarioTitulo(usuario);
        analise.setObservacoes(observacoes);
        repositorioAnaliseCadastro.save(analise);

        Unidade unidadeOrigem = sp.getUnidade();
        Unidade unidadeDestino = unidadeOrigem.getUnidadeSuperior();
        if (unidadeDestino == null) {
            throw new IllegalStateException("Não foi possível identificar a unidade superior para enviar a análise.");
        }

        repositorioMovimentacao.save(new Movimentacao(sp, unidadeOrigem, unidadeDestino, "Cadastro de atividades e conhecimentos aceito"));
        log.info("Notificando unidade {} sobre aceite do cadastro da unidade {}", unidadeDestino.getSigla(), unidadeOrigem.getSigla());

        // Notificar unidade superior
        notificarAceiteCadastro(sp, unidadeDestino);

        return subprocessoMapper.toDTO(sp);
    }

    private void notificarAceiteCadastro(Subprocesso sp, Unidade unidadeDestino) {
        if (unidadeDestino == null || sp.getUnidade() == null) return;

        String siglaUnidadeOrigem = sp.getUnidade().getSigla();
        String nomeProcesso = sp.getProcesso().getDescricao();

        // 1. Enviar E-mail
        String assunto = "SGC: Cadastro da unidade " + siglaUnidadeOrigem + " aceito e aguardando homologação";
        String corpo = String.format(
            "O cadastro de atividades e conhecimentos da unidade %s, referente ao processo '%s', foi aceito e está disponível para homologação.",
            siglaUnidadeOrigem,
            nomeProcesso
        );
        notificacaoService.enviarEmail(unidadeDestino.getSigla(), assunto, corpo);

        // 2. Criar Alerta
        Alerta alerta = new Alerta();
        alerta.setDescricao("Cadastro da unidade " + siglaUnidadeOrigem + " aguardando homologação");
        alerta.setProcesso(sp.getProcesso());
        alerta.setDataHora(java.time.LocalDateTime.now());
        alerta.setUnidadeOrigem(sp.getUnidade());
        alerta.setUnidadeDestino(unidadeDestino);
        repositorioAlerta.save(alerta);
    }

    @Transactional
    public SubprocessoDto homologarCadastro(Long idSubprocesso, String observacoes, String usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (sp.getSituacao() != SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO) {
            throw new IllegalStateException("Ação de homologar só pode ser executada em cadastros disponibilizados.");
        }

        Unidade unidadeOrigemMovimentacao = sp.getUnidade().getUnidadeSuperior();
        // A homologação é uma ação final do ADMIN (SEDOC), a movimentação termina na própria unidade de origem.
        repositorioMovimentacao.save(new Movimentacao(sp, unidadeOrigemMovimentacao, unidadeOrigemMovimentacao, "Cadastro de atividades e conhecimentos homologado"));
        sp.setSituacao(SituacaoSubprocesso.CADASTRO_HOMOLOGADO);
        repositorioSubprocesso.save(sp);

        log.info("Subprocesso {} homologado com sucesso.", idSubprocesso);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDto devolverRevisaoCadastro(Long idSubprocesso, String motivo, String observacoes, String usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA) {
            throw new IllegalStateException("Ação de devolução só pode ser executada em revisões de cadastro disponibilizadas.");
        }

        AnaliseCadastro analise = new AnaliseCadastro();
        analise.setSubprocesso(sp);
        analise.setDataHora(java.time.LocalDateTime.now());
        analise.setAcao(TipoAcaoAnalise.DEVOLUCAO_REVISAO);
        analise.setMotivo(motivo);
        analise.setObservacoes(observacoes);
        analise.setAnalistaUsuarioTitulo(usuario);
        repositorioAnaliseCadastro.save(analise);

        Unidade unidadeDevolucao = sp.getUnidade();
        repositorioMovimentacao.save(new Movimentacao(sp, unidadeDevolucao.getUnidadeSuperior(), unidadeDevolucao, "Devolução da revisão do cadastro para ajustes: " + motivo));
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        sp.setDataFimEtapa1(null);
        repositorioSubprocesso.save(sp);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDto aceitarRevisaoCadastro(Long idSubprocesso, String observacoes, String usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA) {
            throw new IllegalStateException("Ação de aceite só pode ser executada em revisões de cadastro disponibilizadas.");
        }

        AnaliseCadastro analise = new AnaliseCadastro();
        analise.setSubprocesso(sp);
        analise.setDataHora(java.time.LocalDateTime.now());
        analise.setAcao(TipoAcaoAnalise.ACEITE_REVISAO);
        analise.setObservacoes(observacoes);
        analise.setAnalistaUsuarioTitulo(usuario);
        repositorioAnaliseCadastro.save(analise);

        Unidade unidadeOrigem = sp.getUnidade();
        Unidade unidadeDestino = unidadeOrigem.getUnidadeSuperior();

        repositorioMovimentacao.save(new Movimentacao(sp, unidadeOrigem, unidadeDestino, "Revisão do cadastro de atividades e conhecimentos aceita"));

        notificarAceiteRevisaoCadastro(sp, unidadeDestino);

        return subprocessoMapper.toDTO(sp);
    }

    private void notificarAceiteRevisaoCadastro(Subprocesso sp, Unidade unidadeDestino) {
        if (unidadeDestino == null || sp.getUnidade() == null) return;

        String siglaUnidadeOrigem = sp.getUnidade().getSigla();
        String nomeProcesso = sp.getProcesso().getDescricao();

        // 1. Enviar E-mail
        String assunto = "SGC: Revisão de cadastro da " + siglaUnidadeOrigem + " aceita e aguardando homologação";
        String corpo = String.format(
            "A revisão do cadastro de atividades e conhecimentos da unidade %s, referente ao processo '%s', foi aceita e está disponível para homologação.",
            siglaUnidadeOrigem,
            nomeProcesso
        );
        notificacaoService.enviarEmail(unidadeDestino.getSigla(), assunto, corpo);

        // 2. Criar Alerta
        Alerta alerta = new Alerta();
        alerta.setDescricao("Revisão de cadastro da unidade " + siglaUnidadeOrigem + " aguardando homologação");
        alerta.setProcesso(sp.getProcesso());
        alerta.setDataHora(java.time.LocalDateTime.now());
        alerta.setUnidadeOrigem(sp.getUnidade());
        alerta.setUnidadeDestino(unidadeDestino);
        repositorioAlerta.save(alerta);
    }

    @Transactional
    public SubprocessoDto homologarRevisaoCadastro(Long idSubprocesso, String observacoes, String usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA) {
            throw new IllegalStateException("Ação de homologar só pode ser executada em revisões de cadastro disponibilizadas.");
        }

        Unidade unidadeOrigemMovimentacao = sp.getUnidade().getUnidadeSuperior();
        repositorioMovimentacao.save(new Movimentacao(sp, unidadeOrigemMovimentacao, unidadeOrigemMovimentacao, "Revisão do cadastro de atividades e conhecimentos homologada"));
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        repositorioSubprocesso.save(sp);

        log.info("Revisão do subprocesso {} homologada com sucesso.", idSubprocesso);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public void importarAtividades(Long idSubprocessoDestino, Long idSubprocessoOrigem) {
        Subprocesso spDestino = repositorioSubprocesso.findById(idSubprocessoDestino)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso de destino não encontrado: " + idSubprocessoDestino));

        if (spDestino.getSituacao() != SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO) {
            throw new IllegalStateException("Atividades só podem ser importadas para um subprocesso com cadastro em elaboração.");
        }

        Subprocesso spOrigem = repositorioSubprocesso.findById(idSubprocessoOrigem)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso de origem não encontrado: " + idSubprocessoOrigem));

        if (spOrigem.getMapa() == null || spDestino.getMapa() == null) {
            throw new IllegalStateException("Subprocesso de origem ou destino não possui mapa associado.");
        }

        List<Atividade> atividadesOrigem = atividadeRepo.findByMapaCodigo(spOrigem.getMapa().getCodigo());
        if (atividadesOrigem == null || atividadesOrigem.isEmpty()) {
            return; // Nada a importar
        }

        for (Atividade atividadeOrigem : atividadesOrigem) {
            Atividade novaAtividade = new Atividade();
            novaAtividade.setDescricao(atividadeOrigem.getDescricao());
            novaAtividade.setMapa(spDestino.getMapa());
            Atividade atividadeSalva = atividadeRepo.save(novaAtividade);

            List<Conhecimento> conhecimentosOrigem = repositorioConhecimento.findByAtividadeCodigo(atividadeOrigem.getCodigo());
            if (conhecimentosOrigem != null) {
                for (Conhecimento conhecimentoOrigem : conhecimentosOrigem) {
                    Conhecimento novoConhecimento = new Conhecimento();
                    novoConhecimento.setDescricao(conhecimentoOrigem.getDescricao());
                    novoConhecimento.setAtividade(atividadeSalva);
                    repositorioConhecimento.save(novoConhecimento);
                }
            }
        }

        String descricaoMovimentacao = String.format("Importação de atividades do subprocesso #%d (Unidade: %s)",
            spOrigem.getCodigo(),
            spOrigem.getUnidade() != null ? spOrigem.getUnidade().getSigla() : "N/A");

        repositorioMovimentacao.save(new Movimentacao(spDestino, spDestino.getUnidade(), spDestino.getUnidade(), descricaoMovimentacao));

        log.info("Atividades importadas com sucesso do subprocesso {} para {}", idSubprocessoOrigem, idSubprocessoDestino);
    }
}

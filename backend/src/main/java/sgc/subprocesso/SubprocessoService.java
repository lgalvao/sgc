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
import sgc.analise.modelo.AnaliseValidacao;
import sgc.analise.modelo.AnaliseValidacaoRepo;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
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
        SubprocessoDetalheDto dto = new SubprocessoDetalheDto();

        if (sp.getUnidade() != null) {
            dto.setUnidade(new SubprocessoDetalheDto.UnidadeDTO(sp.getUnidade().getCodigo(), sp.getUnidade().getSigla(), sp.getUnidade().getNome()));
        }

        if (sp.getUnidade() != null && sp.getUnidade().getTitular() != null) {
            var titular = sp.getUnidade().getTitular();
            dto.setResponsavel(new SubprocessoDetalheDto.ResponsavelDTO(null, titular.getNome(), null, titular.getRamal(), titular.getEmail()));
        }

        dto.setSituacao(sp.getSituacaoId());

        if (movimentacoes != null && !movimentacoes.isEmpty()) {
            Movimentacao m = movimentacoes.getFirst();
            if (m.getUnidadeDestino() != null) {
                dto.setLocalizacaoAtual(m.getUnidadeDestino().getSigla());
            }
        }

        dto.setPrazoEtapaAtual(sp.getDataLimiteEtapa1() != null ? sp.getDataLimiteEtapa1() : sp.getDataLimiteEtapa2());

        if (movimentacoes != null) {
            dto.setMovimentacoes(movimentacoes.stream().map(movimentacaoMapper::toDTO).collect(Collectors.toList()));
        }

        List<SubprocessoDetalheDto.ElementoProcessoDTO> elementos = new ArrayList<>();
        if (atividades != null) {
            atividades.forEach(a -> elementos.add(new SubprocessoDetalheDto.ElementoProcessoDTO("ATIVIDADE", atividadeMapper.toDTO(a))));
        }
        if (conhecimentos != null) {
            conhecimentos.forEach(c -> elementos.add(new SubprocessoDetalheDto.ElementoProcessoDTO("CONHECIMENTO", conhecimentoMapper.toDTO(c))));
        }
        dto.setElementosDoProcesso(elementos);

        return dto;
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

        sp.setSituacaoId("CADASTRO_DISPONIBILIZADO");
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

        sp.setSituacaoId("REVISAO_CADASTRO_DISPONIBILIZADA");
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

        sp.setSituacaoId("MAPA_DISPONIBILIZADO");
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
        sp.setSituacaoId("MAPA_COM_SUGESTOES");
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

        sp.setSituacaoId("MAPA_VALIDADO");
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

        sp.setSituacaoId("MAPA_DISPONIBILIZADO");
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

        sp.setSituacaoId("MAPA_HOMOLOGADO");
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

        String justificativa = "Ajustes necessários conforme análise anterior.";

        return new MapaAjusteDto(idMapa, nomeUnidade, new ArrayList<>(), justificativa);
    }

    @Transactional
    public SubprocessoDto salvarAjustesMapa(Long idSubprocesso, List<?> competencias, String usuarioTitulo) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (!"REVISAO_CADASTRO_HOMOLOGADA".equals(sp.getSituacaoId())) {
            throw new IllegalStateException("Ajustes no mapa só podem ser feitos quando a revisão do cadastro está homologada.");
        }

        log.info("Salvando ajustes para o mapa do subprocesso {}...", idSubprocesso);

        sp.setSituacaoId("MAPA_AJUSTADO");
        repositorioSubprocesso.save(sp);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDto submeterMapaAjustado(Long idSubprocesso, String usuarioTitulo) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        sp.setSituacaoId("MAPA_AJUSTADO");
        repositorioSubprocesso.save(sp);

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade(), sp.getUnidade().getUnidadeSuperior(), "Submissão do mapa ajustado"));

        return subprocessoMapper.toDTO(sp);
    }

    private void notificarDisponibilizacaoMapa(Subprocesso sp) {
        Unidade unidade = sp.getUnidade();
        if (unidade == null) return;

        String assunto = "SGC: Mapa de competências disponibilizado para validação";
        String corpo = String.format(
            "O mapa de competências da unidade %s foi disponibilizado para validação no processo '%s'. O prazo para conclusão desta etapa é %s.",
            unidade.getSigla(),
            sp.getProcesso().getDescricao(),
            sp.getDataLimiteEtapa2()
        );

        notificacaoService.enviarEmail(unidade.getSigla(), assunto, corpo);

        Unidade superior = unidade.getUnidadeSuperior();
        while (superior != null) {
            notificacaoService.enviarEmail(superior.getSigla(), assunto, corpo);
            superior = superior.getUnidadeSuperior();
        }

        Alerta alerta = new Alerta();
        alerta.setDescricao("Mapa de competências da unidade " + unidade.getSigla() + " disponibilizado para análise");
        alerta.setProcesso(sp.getProcesso());
        alerta.setDataHora(java.time.LocalDateTime.now());
        alerta.setUnidadeOrigem(null);
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
        sp.setSituacaoId("CADASTRO_EM_ELABORACAO");
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

        if (!"CADASTRO_DISPONIBILIZADO".equals(sp.getSituacaoId())) {
            throw new IllegalStateException("Ação de aceite só pode ser executada em cadastros disponibilizados.");
        }

        AnaliseCadastro analise = new AnaliseCadastro();
        analise.setSubprocesso(sp);
        analise.setDataHora(java.time.LocalDateTime.now());
        analise.setAcao("ACEITE");
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

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDto homologarCadastro(Long idSubprocesso, String observacoes, String usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (!"CADASTRO_DISPONIBILIZADO".equals(sp.getSituacaoId())) {
            throw new IllegalStateException("Ação de homologar só pode ser executada em cadastros disponibilizados.");
        }

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade().getUnidadeSuperior(), sp.getUnidade().getUnidadeSuperior(), "Cadastro de atividades e conhecimentos homologado"));
        sp.setSituacaoId("CADASTRO_HOMOLOGADO");
        repositorioSubprocesso.save(sp);

        log.info("Subprocesso {} homologado com sucesso.", idSubprocesso);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDto devolverRevisaoCadastro(Long idSubprocesso, String motivo, String observacoes, String usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

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
        repositorioAnaliseCadastro.save(analise);

        Unidade unidadeDevolucao = sp.getUnidade();
        repositorioMovimentacao.save(new Movimentacao(sp, unidadeDevolucao.getUnidadeSuperior(), unidadeDevolucao, "Devolução da revisão do cadastro para ajustes: " + motivo));
        sp.setSituacaoId("REVISAO_CADASTRO_EM_ANDAMENTO");
        sp.setDataFimEtapa1(null);
        repositorioSubprocesso.save(sp);

        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDto aceitarRevisaoCadastro(Long idSubprocesso, String observacoes, String usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (!"REVISAO_CADASTRO_DISPONIBILIZADA".equals(sp.getSituacaoId())) {
            throw new IllegalStateException("Ação de aceite só pode ser executada em revisões de cadastro disponibilizadas.");
        }

        AnaliseCadastro analise = new AnaliseCadastro();
        analise.setSubprocesso(sp);
        analise.setDataHora(java.time.LocalDateTime.now());
        analise.setAcao("ACEITE_REVISAO");
        analise.setObservacoes(observacoes);
        analise.setAnalistaUsuarioTitulo(usuario);
        repositorioAnaliseCadastro.save(analise);

        Unidade unidadeOrigem = sp.getUnidade();
        Unidade unidadeDestino = unidadeOrigem.getUnidadeSuperior();

        repositorioMovimentacao.save(new Movimentacao(sp, unidadeOrigem, unidadeDestino, "Revisão do cadastro de atividades e conhecimentos aceita"));
        return subprocessoMapper.toDTO(sp);
    }

    @Transactional
    public SubprocessoDto homologarRevisaoCadastro(Long idSubprocesso, String observacoes, String usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (!"REVISAO_CADASTRO_DISPONIBILIZADA".equals(sp.getSituacaoId())) {
            throw new IllegalStateException("Ação de homologar só pode ser executada em revisões de cadastro disponibilizadas.");
        }

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade().getUnidadeSuperior(), sp.getUnidade().getUnidadeSuperior(), "Revisão do cadastro de atividades e conhecimentos homologada"));
        sp.setSituacaoId("REVISAO_CADASTRO_HOMOLOGADA");
        repositorioSubprocesso.save(sp);

        return subprocessoMapper.toDTO(sp);
    }
}
package sgc.processo.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.*;
import sgc.alerta.model.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.dto.*;
import sgc.processo.dto.ProcessoDetalheDto.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;

import static sgc.processo.model.AcaoProcesso.*;
import static sgc.processo.model.SituacaoProcesso.*;
import static sgc.processo.model.TipoProcesso.*;
import static sgc.seguranca.AcaoPermissao.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

/**
 * Serviço consolidado para o domínio de Processos.
 * Unifica Consulta, Manutenção, Workflow, Validação e Notificação.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ProcessoService {
    private static final String SIGLA_UNIDADE_ADMIN = "ADMIN";
    private static final String SIGLA_UNIDADE_SEDOC = "SEDOC";

    private final ProcessoRepo processoRepo;
    private final ComumRepo repo;
    private final UnidadeHierarquiaService unidadeHierarquiaService;
    private final UnidadeService unidadeService;
    private final ResponsavelUnidadeService responsavelUnidadeService;
    private final SubprocessoService subprocessoService;
    private final SubprocessoConsultaService consultaService;
    private final LocalizacaoSubprocessoService localizacaoSubprocessoService;
    private final SubprocessoValidacaoService validacaoService;
    private final UsuarioFacade usuarioService;
    private final AlertaFacade servicoAlertas;
    private final NotificacaoService notificacaoService;
    private final EmailModelosService emailModelosService;
    private final SgcPermissionEvaluator permissionEvaluator;
    private final SubprocessoTransicaoService transicaoService;
    private final CadastroFluxoService cadastroFluxoService;


    @Transactional(readOnly = true)
    public Processo buscarPorCodigo(Long codigo) {
        return repo.buscar(Processo.class, codigo);
    }

    @Transactional(readOnly = true)
    public Processo buscarPorCodigoComParticipantes(Long codigo) {
        return processoRepo.buscarPorCodigoComParticipantes(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));
    }

    @Transactional(readOnly = true)
    public Optional<Processo> buscarOpt(Long codigo) {
        return processoRepo.buscarPorCodigoComParticipantes(codigo);
    }

    @Transactional(readOnly = true)
    public List<Processo> listarFinalizados() {
        Usuario usuario = usuarioService.usuarioAutenticado();
        if (usuario.getPerfilAtivo() == Perfil.ADMIN) {
            return processoRepo.listarPorSituacaoComParticipantes(FINALIZADO);
        }
        List<Long> unidadesAcesso = buscarCodigosAcesso(usuario);
        return processoRepo.listarPorSituacaoEUnidadeCodigos(FINALIZADO, unidadesAcesso);
    }

    @Transactional(readOnly = true)
    public List<Processo> listarParaImportacao() {
        return processoRepo.listarPorSituacaoComParticipantes(FINALIZADO);
    }

    @Transactional(readOnly = true)
    public List<Processo> listarAtivos() {
        Usuario usuario = usuarioService.usuarioAutenticado();
        if (usuario.getPerfilAtivo() == Perfil.ADMIN) {
            return processoRepo.listarPorSituacao(EM_ANDAMENTO);
        }
        List<Long> unidadesAcesso = buscarCodigosAcesso(usuario);
        return processoRepo.listarPorSituacaoEUnidadeCodigos(EM_ANDAMENTO, unidadesAcesso);
    }

    @Transactional(readOnly = true)
    public Page<Processo> listarTodos(Pageable pageable) {
        Page<Long> paginaCodigos = processoRepo.listarCodigos(pageable);
        return carregarPaginaComParticipantes(paginaCodigos, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Processo> listarIniciadosPorParticipantes(List<Long> unidadeCodigos, Pageable pageable) {
        Page<Long> paginaCodigos = processoRepo.listarCodigosPorParticipantesESituacaoDiferente(
                unidadeCodigos, CRIADO, pageable);
        return carregarPaginaComParticipantes(paginaCodigos, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Processo> listarIniciadosPorSubprocessos(List<Long> unidadeCodigos, Pageable pageable) {
        Page<Long> paginaCodigos = processoRepo.listarCodigosPorSubprocessosESituacaoDiferente(
                unidadeCodigos, CRIADO, pageable);
        return carregarPaginaComParticipantes(paginaCodigos, pageable);
    }

    @Transactional(readOnly = true)
    public List<Long> listarUnidadesBloqueadasPorTipo(TipoProcesso tipo) {
        return processoRepo.listarUnidadesBloqueadasPorSituacaoETipo(EM_ANDAMENTO, tipo);
    }

    @Transactional(readOnly = true)
    public Set<Long> buscarIdsUnidadesComProcessosAtivos(Long codProcessoIgnorar) {
        return new HashSet<>(processoRepo.listarUnidadesEmSituacoesExcetoProcesso(
                Arrays.asList(EM_ANDAMENTO, CRIADO), codProcessoIgnorar));
    }

    @Transactional(readOnly = true)
    public List<SubprocessoElegivelDto> listarSubprocessosElegiveis(Long codProcesso) {
        Usuario usuario = usuarioService.usuarioAutenticado();
        List<Subprocesso> subprocessos;
        if (usuario.getPerfilAtivo() == Perfil.ADMIN) {
            subprocessos = consultaService.listarEntidadesPorProcesso(codProcesso);
        } else {
            List<Long> unidadesAcesso = buscarCodigosAcesso(usuario);
            subprocessos = consultaService.listarEntidadesPorProcessoEUnidades(codProcesso, unidadesAcesso);
        }

        return listarSubprocessosElegiveis(subprocessos, usuario, false, Map.of());
    }

    private List<SubprocessoElegivelDto> listarSubprocessosElegiveis(
            List<Subprocesso> subprocessos,
            Usuario usuario,
            boolean usarLocalizacoesPrecarregadas,
            Map<Long, Unidade> localizacoesPrecarregadas
    ) {
        List<Subprocesso> subprocessosElegiveis = new ArrayList<>();
        Map<Long, ElegibilidadeAcaoBloco> elegibilidadesPorSubprocesso = new HashMap<>();

        for (Subprocesso subprocesso : subprocessos) {
            ElegibilidadeAcaoBloco elegibilidade = avaliarElegibilidadeAcaoBloco(
                    subprocesso,
                    usuario,
                    usarLocalizacoesPrecarregadas,
                    localizacoesPrecarregadas
            );
            if (!elegibilidade.possuiAlgumaAcao()) {
                continue;
            }
            subprocessosElegiveis.add(subprocesso);
            elegibilidadesPorSubprocesso.put(subprocesso.getCodigo(), elegibilidade);
        }

        Map<Long, Unidade> localizacoesPorSubprocesso = usarLocalizacoesPrecarregadas
                ? localizacoesPrecarregadas
                : localizacaoSubprocessoService.obterLocalizacoesAtuais(subprocessosElegiveis);

        return subprocessosElegiveis.stream()
                .map(subprocesso -> toElegivelDto(
                        subprocesso,
                        obterLocalizacaoAtual(subprocesso, localizacoesPorSubprocesso),
                        Objects.requireNonNull(
                                elegibilidadesPorSubprocesso.get(subprocesso.getCodigo()),
                                "Elegibilidade obrigatoria para subprocesso elegivel"
                        )
                ))
                .toList();
    }


    public Processo criar(CriarProcessoRequest req) {
        List<Long> codigosUnidades = new ArrayList<>(req.unidades());
        Map<Long, Unidade> unidadesPorCodigo = carregarUnidadesPorCodigo(codigosUnidades);
        validarUnidadesParaProcesso(req.tipo(), unidadesPorCodigo);

        Set<Unidade> participantes = new HashSet<>(unidadesPorCodigo.values());

        Processo processo = new Processo()
                .setDescricao(req.descricao())
                .setTipo(req.tipo())
                .setDataLimite(req.dataLimiteEtapa1())
                .setSituacao(CRIADO)
                .setDataCriacao(LocalDateTime.now());

        processo.adicionarParticipantes(participantes);
        log.info("Processo criado com {} unidades participantes", participantes.size());
        return processoRepo.saveAndFlush(processo);
    }

    public Processo atualizar(Long codigo, AtualizarProcessoRequest req) {
        Processo processo = buscarPorCodigo(codigo);
        if (processo.getSituacao() != CRIADO) {
            throw new ErroValidacao(Mensagens.PROCESSO_SO_EDITAVEL_EM_CRIADO);
        }

        Map<Long, Unidade> unidadesPorCodigo = carregarUnidadesPorCodigo(new ArrayList<>(req.unidades()));
        validarUnidadesParaProcesso(req.tipo(), unidadesPorCodigo);

        Set<Unidade> participantes = new HashSet<>(unidadesPorCodigo.values());

        processo.setDescricao(req.descricao());
        processo.setTipo(req.tipo());
        processo.setDataLimite(req.dataLimiteEtapa1());
        processo.sincronizarParticipantes(participantes);

        log.info("Processo {} atualizado.", codigo);
        return processoRepo.saveAndFlush(processo);
    }

    public void apagar(Long codigo) {
        Processo processo = buscarPorCodigo(codigo);
        if (processo.getSituacao() != CRIADO) {
            throw new ErroValidacao(Mensagens.PROCESSO_SO_REMOVIVEL_EM_CRIADO);
        }
        processoRepo.deleteById(codigo);
        log.info("Processo {} removido.", codigo);
    }


    public void iniciar(Long codigo, List<Long> codsUnidadesParam) {
        Processo processo = buscarPorCodigo(codigo);
        if (processo.getSituacao() != CRIADO) {
            throw new ErroValidacao(Mensagens.PROCESSO_SO_INICIAVEL_EM_CRIADO);
        }

        ContextoInicioProcesso contexto = resolverContextoInicio(processo, codsUnidadesParam);
        validarInicioSemErros(contexto);

        efetivarInicioSubprocessos(new InicioSubprocessosContexto(
                processo,
                contexto.tipo(),
                contexto.codigosUnidades(),
                contexto.unidadesParaProcessar(),
                carregarMapasParaInicio(contexto),
                unidadeService.buscarAdmin()
        ));

        persistirProcessoIniciado(processo);
        List<Unidade> participantes = new ArrayList<>(contexto.unidadesParaProcessar());
        criarAlertasInicioProcesso(processo, participantes);
        criarNotificacoesInicioProcesso(processo, participantes);

        log.info("Processo {} iniciado para {} unidades.", codigo, contexto.codigosUnidades().size());
    }

    public void finalizar(Long codigo) {
        Processo processo = buscarPorCodigo(codigo);
        validarFinalizacao(codigo, processo);

        if (processo.getTipo() != DIAGNOSTICO) {
            tornarMapasVigentes(codigo);
        }
        criarAlertasFinalizacaoProcesso(processo);
        criarNotificacoesFinalizacaoProcesso(processo);

        processo.setSituacao(FINALIZADO);
        processo.setDataFinalizacao(LocalDateTime.now());
        processoRepo.save(processo);


        log.info("Processo {} finalizado", codigo);
    }


    public void executarAcaoEmBloco(Long codProcesso, AcaoEmBlocoCommand command) {
        Usuario usuario = usuarioService.usuarioAutenticado();
        List<Long> unidadeCodigos = command.unidadeCodigos();
        List<Subprocesso> subprocessos = consultaService.listarEntidadesPorProcessoEUnidades(codProcesso, unidadeCodigos);

        if (unidadeCodigos.isEmpty()) throw new ErroValidacao(Mensagens.SELECIONE_AO_MENOS_UMA_UNIDADE);
        validarSelecaoBloco(unidadeCodigos, subprocessos);

        if (command instanceof DisponibilizarMapaEmBlocoCommand disponibilizacao) {
            executarDisponibilizacaoMapaEmBloco(disponibilizacao, usuario, subprocessos);
            return;
        }

        if (command instanceof ProcessarAnaliseEmBlocoCommand analise) {
            validarPermissaoAnaliseEmBloco(usuario, subprocessos, analise);
            processarAcoesBlocoAceiteHomologacao(analise, subprocessos);
        }
    }

    private void validarPermissaoAnaliseEmBloco(
            Usuario usuario,
            List<Subprocesso> subprocessos,
            ProcessarAnaliseEmBlocoCommand analise
    ) {
        AcaoPermissao acaoRequerida = switch (analise.acao()) {
            case ACEITAR -> ACEITAR_MAPA;
            case HOMOLOGAR -> HOMOLOGAR_MAPA;
            default -> null;
        };
        if (acaoRequerida == null) {
            return;
        }
        if (!permissionEvaluator.verificarPermissao(usuario, subprocessos, acaoRequerida)) {
            throw new ErroAcessoNegado("Usuário não possui permissão para executar esta ação em um ou mais subprocessos selecionados.");
        }
    }


    @Transactional(readOnly = true)
    public ProcessoDetalheDto obterDetalhesCompleto(Long codProcesso, boolean incluirElegiveis) {
        Usuario usuario = usuarioService.usuarioAutenticado();
        Processo processo = buscarPorCodigo(codProcesso);
        List<Subprocesso> subprocessos = consultaService.listarEntidadesPorProcesso(codProcesso);
        Map<Long, Unidade> localizacoesPorSubprocesso = localizacaoSubprocessoService.obterLocalizacoesAtuais(subprocessos);
        Set<Long> unidadesAcesso = obterIdsUnidadesAcesso(processo, usuario);
        Perfil perfil = usuario.getPerfilAtivo();

        ProcessoDetalheDto dto = ProcessoDetalheDto.builder()
                .codigo(processo.getCodigo())
                .descricao(processo.getDescricao())
                .situacao(processo.getSituacao())
                .tipo(processo.getTipo().name())
                .dataCriacao(processo.getDataCriacao())
                .dataFinalizacao(processo.getDataFinalizacao())
                .dataLimite(processo.getDataLimite())
                .podeFinalizar(permissionEvaluator.verificarPermissao(usuario, processo, FINALIZAR_PROCESSO)
                        && validacaoService.validarSubprocessosParaFinalizacao(codProcesso).valido())
                .podeHomologarCadastro(AcaoPermissao.HOMOLOGAR_CADASTRO_EM_BLOCO.permitePerfil(perfil))
                .podeHomologarMapa(AcaoPermissao.HOMOLOGAR_MAPA_EM_BLOCO.permitePerfil(perfil))
                .podeAceitarCadastroBloco(AcaoPermissao.ACEITAR_CADASTRO_EM_BLOCO.permitePerfil(perfil))
                .podeAceitarMapaBloco(AcaoPermissao.ACEITAR_MAPA_EM_BLOCO.permitePerfil(perfil))
                .podeDisponibilizarMapaBloco(AcaoPermissao.DISPONIBILIZAR_MAPA_EM_BLOCO.permitePerfil(perfil))
                .unidades(new ArrayList<>())
                .build();

        montarHierarquiaNoDto(dto, processo, subprocessos, unidadesAcesso, localizacoesPorSubprocesso);
        List<Subprocesso> subprocessosVisiveis = usuario.getPerfilAtivo() == Perfil.ADMIN
                ? subprocessos
                : subprocessos.stream()
                .filter(subprocesso -> unidadesAcesso.contains(subprocesso.getUnidade().getCodigo()))
                .toList();
        if (incluirElegiveis) {
            List<SubprocessoElegivelDto> subprocessosElegiveis = listarSubprocessosElegiveis(
                    subprocessosVisiveis,
                    usuario,
                    true,
                    localizacoesPorSubprocesso
            );
            dto.getElegiveis().addAll(subprocessosElegiveis);
            dto.getAcoesBloco().addAll(montarAcoesBloco(processo, subprocessosElegiveis, perfil));
        }

        return dto;
    }


    public void enviarLembrete(Long codProcesso, Long unidadeCodigo) {
        Processo processo = buscarPorCodigoComParticipantes(codProcesso);
        Unidade unidade = unidadeService.buscarPorCodigo(unidadeCodigo);

        if (processo.getParticipantes().stream().noneMatch(u -> Objects.equals(u.getUnidadeCodigo(), unidadeCodigo))) {
            throw new ErroValidacao(Mensagens.UNIDADE_NAO_PARTICIPA);
        }

        LocalDateTime dataLimite = obterDataLimiteObrigatoria(processo, codProcesso);
        String dataLimiteText = dataLimite.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String corpoHtml = emailModelosService.criarEmailLembretePrazo(unidade.getSigla(), processo.getDescricao(), dataLimite);

        criarAlertaLembretePrazo(processo, unidade, dataLimiteText);

        String assunto = "SGC: Lembrete - " + processo.getDescricao();
        String chave = "processo:%d:lembrete:unidade:%d:dia:%s"
                .formatted(codProcesso, unidadeCodigo, LocalDate.now());
        enfileirarNotificacaoUnidade(
                unidade,
                TipoNotificacao.LEMBRETE_PRAZO,
                assunto,
                corpoHtml,
                chave,
                null
        );
    }


    private List<Long> buscarCodigosAcesso(Usuario usuario) {
        Long root = usuario.getUnidadeAtivaCodigo();
        if (usuario.getPerfilAtivo() == Perfil.GESTOR) {
            return buscarDescendentes(root);
        }
        return List.of(root);
    }

    private Page<Processo> carregarPaginaComParticipantes(Page<Long> paginaCodigos, Pageable pageable) {
        List<Long> codigos = paginaCodigos.getContent();
        if (codigos.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, paginaCodigos.getTotalElements());
        }

        Map<Long, Processo> processosPorCodigo = processoRepo.listarPorCodigosComParticipantes(codigos).stream()
                .collect(Collectors.toMap(Processo::getCodigo, processo -> processo));

        List<Processo> processosOrdenados = codigos.stream()
                .map(processosPorCodigo::get)
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(processosOrdenados, pageable, paginaCodigos.getTotalElements());
    }

    private List<Long> buscarDescendentes(Long codRaiz) {
        List<Long> descendentes = unidadeHierarquiaService.buscarIdsDescendentes(codRaiz);
        List<Long> resultado = new ArrayList<>(descendentes.size() + 1);
        resultado.add(codRaiz);
        resultado.addAll(descendentes);
        return resultado;
    }

    private void validarUnidadesParaProcesso(TipoProcesso tipo, Map<Long, Unidade> unidadesPorCodigo) {
        List<Unidade> entidades = new ArrayList<>(unidadesPorCodigo.values());
        List<String> invalidas = entidades.stream()
                .filter(u -> u.getTipo() == TipoUnidade.INTERMEDIARIA)
                .map(Unidade::getSigla).toList();
        if (!invalidas.isEmpty())
            throw new ErroValidacao(Mensagens.UNIDADES_INTERMEDIARIA_INVALIDAS.formatted(String.join(", ", invalidas)));

        if (possuiUnidadeSemResponsavelEfetivo(entidades)) {
            throw new ErroValidacao(Mensagens.OPERACAO_NAO_PERMITIDA);
        }

        if (tipo == REVISAO || tipo == DIAGNOSTICO) {
            Set<Long> unidadesComMapa = new HashSet<>(unidadeService.buscarTodosCodigosUnidadesComMapa());
            List<String> semMapa = entidades.stream()
                    .filter(unidade -> !unidadesComMapa.contains(unidade.getCodigo()))
                    .map(Unidade::getSigla)
                    .toList();
            if (!semMapa.isEmpty())
                throw new ErroValidacao(Mensagens.UNIDADES_SEM_MAPA_VIGENTE.formatted(String.join(", ", semMapa)));
        }
    }

    private List<String> validarUnidadesInicio(TipoProcesso tipo, List<Long> cods, Collection<Unidade> unidadesCarregadas) {
        List<String> erros = new ArrayList<>();
        List<Unidade> unidades = new ArrayList<>(unidadesCarregadas);
        if (possuiUnidadeSemResponsavelEfetivo(unidades)) {
            erros.add(Mensagens.OPERACAO_NAO_PERMITIDA);
        }
        if (tipo == REVISAO || tipo == DIAGNOSTICO) {
            Set<Long> unidadesComMapa = new HashSet<>(unidadeService.buscarTodosCodigosUnidadesComMapa());
            unidadeService.buscarSiglasPorCodigos(cods.stream()
                            .filter(codigo -> !unidadesComMapa.contains(codigo))
                            .toList())
                    .stream().findFirst().ifPresent(s -> erros.add(Mensagens.UNIDADES_SEM_MAPA));
        }
        List<Long> bloqueadas = processoRepo.listarUnidadesEmProcessoAtivo(EM_ANDAMENTO, cods);
        if (!bloqueadas.isEmpty()) erros.add(Mensagens.UNIDADES_EM_PROCESSO_ATIVO);
        return erros;
    }

    private void validarInicioSemErros(ContextoInicioProcesso contexto) {
        List<String> erros = validarUnidadesInicio(contexto.tipo(), contexto.codigosUnidades(), contexto.unidadesParaProcessar());
        if (!erros.isEmpty()) {
            throw new ErroValidacao(String.join(", ", erros));
        }
    }

    private List<UnidadeMapa> carregarMapasParaInicio(ContextoInicioProcesso contexto) {
        if (contexto.tipo() != REVISAO && contexto.tipo() != DIAGNOSTICO) {
            return List.of();
        }
        return unidadeService.buscarMapasPorUnidades(contexto.codigosUnidades());
    }

    private void persistirProcessoIniciado(Processo processo) {
        processo.setSituacao(EM_ANDAMENTO);
        processoRepo.save(processo);
    }

    private boolean possuiUnidadeSemResponsavelEfetivo(List<Unidade> unidades) {
        List<Long> codigos = unidades.stream().map(Unidade::getCodigo).toList();
        return !responsavelUnidadeService.todasPossuemResponsavelEfetivo(codigos);
    }

    private void validarFinalizacao(Long codProcesso, Processo processo) {
        if (processo.getSituacao() != EM_ANDAMENTO) throw new ErroValidacao(Mensagens.SITUACAO_INVALIDA);
        if (!validacaoService.validarSubprocessosParaFinalizacao(codProcesso).valido())
            throw new ErroValidacao(Mensagens.SUBPROCESSOS_NAO_HOMOLOGADOS);
    }

    private void tornarMapasVigentes(Long codProcesso) {
        List<Subprocesso> subprocessos = consultaService.listarEntidadesPorProcesso(codProcesso);
        Map<Long, Mapa> mapasPorUnidade = subprocessos.stream()
                .filter(sp -> sp.getMapa() != null)
                .collect(java.util.stream.Collectors.toMap(
                        sp -> sp.getUnidade().getCodigo(),
                        Subprocesso::getMapa,
                        (a, b) -> a
                ));
        unidadeService.definirMapasVigentesEmBloco(mapasPorUnidade);
    }

    private Set<Unidade> carregarArvoreUnidades(Set<Unidade> participantes) {
        Set<Unidade> arvore = new HashSet<>(participantes);
        Set<Long> codigosSuperiores = new HashSet<>();
        for (Unidade u : participantes) {
            codigosSuperiores.addAll(unidadeHierarquiaService.buscarCodigosSuperiores(u.getCodigo()));
        }
        if (!codigosSuperiores.isEmpty()) {
            unidadeService.buscarPorCodigos(new ArrayList<>(codigosSuperiores)).stream()
                    .filter(u -> u.getTipo() != TipoUnidade.RAIZ)
                    .forEach(arvore::add);
        }
        return arvore;
    }

    private void efetivarInicioSubprocessos(InicioSubprocessosContexto contexto) {
        switch (contexto.tipo()) {
            case MAPEAMENTO -> iniciarSubprocessosMapeamento(contexto);
            case REVISAO -> iniciarSubprocessosRevisao(contexto);
            case DIAGNOSTICO -> iniciarSubprocessosDiagnostico(contexto);
        }
    }


    private void iniciarSubprocessosMapeamento(InicioSubprocessosContexto contexto) {
        subprocessoService.criarParaMapeamento(new SubprocessoService.CriarSubprocessosMapeamentoCommand(
                contexto.processo(),
                contexto.unidadesParaProcessar(),
                contexto.unidadeAdmin()
        ));
    }

    private void iniciarSubprocessosRevisao(InicioSubprocessosContexto contexto) {
        Map<Long, UnidadeMapa> mapasPorUnidade = mapearMapasPorUnidade(contexto.unidadesMapas());
        Map<Long, Unidade> unidadesPorCodigo = mapearUnidadesPorCodigo(contexto.unidadesParaProcessar());

        contexto.codigosUnidades().forEach(codigoUnidade -> subprocessoService.criarParaRevisao(
                new SubprocessoService.CriarSubprocessoComMapaCommand(
                        contexto.processo(),
                        obterUnidadeObrigatoria(unidadesPorCodigo, codigoUnidade),
                        obterUnidadeMapaObrigatorio(mapasPorUnidade, codigoUnidade),
                        contexto.unidadeAdmin()
                )
        ));
    }

    private void iniciarSubprocessosDiagnostico(InicioSubprocessosContexto contexto) {
        Map<Long, UnidadeMapa> mapasPorUnidade = mapearMapasPorUnidade(contexto.unidadesMapas());

        contexto.unidadesParaProcessar().forEach(unidade -> subprocessoService.criarParaDiagnostico(
                new SubprocessoService.CriarSubprocessoComMapaCommand(
                        contexto.processo(),
                        unidade,
                        obterUnidadeMapaObrigatorio(mapasPorUnidade, unidade.getCodigo()),
                        contexto.unidadeAdmin()
                )
        ));
    }

    private Map<Long, UnidadeMapa> mapearMapasPorUnidade(List<UnidadeMapa> unidadesMapas) {
        return unidadesMapas.stream()
                .collect(Collectors.toMap(UnidadeMapa::getUnidadeCodigoPersistido, mapa -> mapa));
    }

    private Map<Long, Unidade> mapearUnidadesPorCodigo(Collection<Unidade> unidades) {
        return unidades.stream()
                .collect(Collectors.toMap(Unidade::getCodigo, unidade -> unidade));
    }

    private void montarHierarquiaNoDto(
            ProcessoDetalheDto dto,
            Processo processo,
            List<Subprocesso> subps,
            Set<Long> acesso,
            Map<Long, Unidade> localizacoesPorSubprocesso
    ) {
        Map<Long, UnidadeParticipanteDto> mapDto = new HashMap<>();
        Map<Long, Subprocesso> mapSp = subps.stream().collect(Collectors.toMap(s -> s.getUnidade().getCodigo(), s -> s));

        processo.getParticipantes().stream()
                .filter(p -> acesso.contains(p.getUnidadeCodigoPersistido()))
                .forEach(p -> {
                    Long codigoUnidadeParticipante = p.getUnidadeCodigoPersistido();
                    UnidadeParticipanteDto uDto = UnidadeParticipanteDto.fromSnapshot(p);
                    Subprocesso sp = mapSp.get(codigoUnidadeParticipante);
                    validarDadosBasicosParticipante(processo.getCodigo(), uDto);
                    if (sp != null) {
                        uDto.preencherComSubprocesso(sp, obterLocalizacaoAtual(sp, localizacoesPorSubprocesso));
                    }
                    mapDto.put(codigoUnidadeParticipante, uDto);
                });

        mapDto.values().forEach(u -> {
            UnidadeParticipanteDto pai = mapDto.get(u.getCodUnidadeSuperior());
            if (pai != null) pai.getFilhos().add(u);
            else dto.getUnidades().add(u);
        });
        Comparator<UnidadeParticipanteDto> comparadorPorSigla =
                Comparator.comparing(UnidadeParticipanteDto::getSigla,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        dto.getUnidades().sort(comparadorPorSigla);
        mapDto.values().forEach(u -> u.getFilhos().sort(comparadorPorSigla));
    }

    private void validarDadosBasicosParticipante(Long codigoProcesso, UnidadeParticipanteDto unidadeDto) {
        if (unidadeDto.getNome().isBlank() || unidadeDto.getSigla().isBlank()) {
            throw new IllegalStateException(
                    "Snapshot inconsistente de unidade participante no processo %d para unidade %d"
                            .formatted(codigoProcesso, unidadeDto.getCodUnidade()));
        }
    }

    private Unidade obterLocalizacao(Subprocesso sp) {
        return localizacaoSubprocessoService.obterLocalizacaoAtual(sp);
    }

    private Unidade obterLocalizacaoAtual(Subprocesso sp, Map<Long, Unidade> localizacoesPorSubprocesso) {
        return Objects.requireNonNullElseGet(localizacoesPorSubprocesso.get(sp.getCodigo()), () -> obterLocalizacao(sp));
    }

    private Set<Long> obterIdsUnidadesAcesso(Processo pr, Usuario us) {
        if (us.getPerfilAtivo() == Perfil.ADMIN) {
            return pr.getParticipantes().stream()
                    .map(UnidadeProcesso::getUnidadeCodigoPersistido)
                    .collect(Collectors.toSet());
        }
        Long root = us.getUnidadeAtivaCodigo();
        if (us.getPerfilAtivo() != Perfil.GESTOR) return Set.of(root);
        Set<Long> subarvore = new HashSet<>(buscarDescendentes(root));
        return pr.getParticipantes().stream()
                .map(UnidadeProcesso::getUnidadeCodigoPersistido)
                .filter(subarvore::contains)
                .collect(Collectors.toSet());
    }

    private boolean podeAceitarCadastroEmBloco(
            Subprocesso subprocesso,
            Usuario usuario,
            boolean usarLocalizacoesPrecarregadas,
            Map<Long, Unidade> localizacoesPrecarregadas
    ) {
        return podeExecutarAcaoEmBloco(
                subprocesso,
                usuario,
                usarLocalizacoesPrecarregadas,
                localizacoesPrecarregadas,
                ACEITAR_CADASTRO,
                this::isSituacaoCadastroDisponibilizado
        );
    }

    private boolean podeAceitarMapaEmBloco(
            Subprocesso subprocesso,
            Usuario usuario,
            boolean usarLocalizacoesPrecarregadas,
            Map<Long, Unidade> localizacoesPrecarregadas
    ) {
        return podeExecutarAcaoEmBloco(
                subprocesso,
                usuario,
                usarLocalizacoesPrecarregadas,
                localizacoesPrecarregadas,
                ACEITAR_MAPA,
                this::isSituacaoMapaAceitavel
        );
    }

    private boolean podeHomologarCadastroEmBloco(
            Subprocesso subprocesso,
            Usuario usuario,
            boolean usarLocalizacoesPrecarregadas,
            Map<Long, Unidade> localizacoesPrecarregadas
    ) {
        return podeExecutarAcaoEmBloco(
                subprocesso,
                usuario,
                usarLocalizacoesPrecarregadas,
                localizacoesPrecarregadas,
                HOMOLOGAR_CADASTRO,
                this::isSituacaoCadastroDisponibilizado
        );
    }

    private boolean podeHomologarMapaEmBloco(
            Subprocesso subprocesso,
            Usuario usuario,
            boolean usarLocalizacoesPrecarregadas,
            Map<Long, Unidade> localizacoesPrecarregadas
    ) {
        return podeExecutarAcaoEmBloco(
                subprocesso,
                usuario,
                usarLocalizacoesPrecarregadas,
                localizacoesPrecarregadas,
                HOMOLOGAR_MAPA,
                this::isSituacaoMapaHomologavel
        );
    }

    private boolean podeDisponibilizarEmBloco(
            Subprocesso subprocesso,
            Usuario usuario,
            boolean usarLocalizacoesPrecarregadas,
            Map<Long, Unidade> localizacoesPrecarregadas
    ) {
        SituacaoSubprocesso situacao = subprocesso.getSituacao();
        boolean elegivelDisponibilizacao = situacao == MAPEAMENTO_MAPA_CRIADO
                || situacao == MAPEAMENTO_MAPA_COM_SUGESTOES
                || situacao == REVISAO_MAPA_COM_SUGESTOES
                || situacao == REVISAO_MAPA_AJUSTADO
                || situacao == REVISAO_CADASTRO_HOMOLOGADA;
        return elegivelDisponibilizacao
                && verificarPermissaoEscritaEmBloco(
                usuario,
                subprocesso,
                DISPONIBILIZAR_MAPA,
                usarLocalizacoesPrecarregadas,
                localizacoesPrecarregadas
        );
    }

    private boolean podeExecutarAcaoEmBloco(
            Subprocesso subprocesso,
            Usuario usuario,
            boolean usarLocalizacoesPrecarregadas,
            Map<Long, Unidade> localizacoesPrecarregadas,
            AcaoPermissao acao,
            java.util.function.Predicate<SituacaoSubprocesso> criterioSituacao
    ) {
        return criterioSituacao.test(subprocesso.getSituacao())
                && verificarPermissaoEscritaEmBloco(
                usuario,
                subprocesso,
                acao,
                usarLocalizacoesPrecarregadas,
                localizacoesPrecarregadas
        );
    }

    private boolean verificarPermissaoEscritaEmBloco(
            Usuario usuario,
            Subprocesso subprocesso,
            AcaoPermissao acao,
            boolean usarLocalizacoesPrecarregadas,
            Map<Long, Unidade> localizacoesPrecarregadas
    ) {
        if (!usarLocalizacoesPrecarregadas) {
            return permissionEvaluator.verificarPermissaoSilenciosa(usuario, subprocesso, acao);
        }

        Processo processo = subprocesso.getProcesso();
        if (!acao.permitePerfil(usuario.getPerfilAtivo()) || (processo != null && processo.getSituacao() == FINALIZADO)) {
            return false;
        }

        Unidade localizacao = obterLocalizacaoAtual(subprocesso, localizacoesPrecarregadas);
        return Objects.equals(usuario.getUnidadeAtivaCodigo(), localizacao.getCodigo());
    }

    @SuppressWarnings("unused")
    private boolean isElegivelParaAcaoEmBloco(Subprocesso subprocesso, Usuario usuario) {
        return avaliarElegibilidadeAcaoBloco(subprocesso, usuario).possuiAlgumaAcao();
    }

    private ElegibilidadeAcaoBloco avaliarElegibilidadeAcaoBloco(Subprocesso subprocesso, Usuario usuario) {
        return avaliarElegibilidadeAcaoBloco(subprocesso, usuario, false, Map.of());
    }

    private ElegibilidadeAcaoBloco avaliarElegibilidadeAcaoBloco(
            Subprocesso subprocesso,
            Usuario usuario,
            boolean usarLocalizacoesPrecarregadas,
            Map<Long, Unidade> localizacoesPrecarregadas
    ) {
        return new ElegibilidadeAcaoBloco(
                podeAceitarCadastroEmBloco(subprocesso, usuario, usarLocalizacoesPrecarregadas, localizacoesPrecarregadas),
                podeAceitarMapaEmBloco(subprocesso, usuario, usarLocalizacoesPrecarregadas, localizacoesPrecarregadas),
                podeHomologarCadastroEmBloco(subprocesso, usuario, usarLocalizacoesPrecarregadas, localizacoesPrecarregadas),
                podeHomologarMapaEmBloco(subprocesso, usuario, usarLocalizacoesPrecarregadas, localizacoesPrecarregadas),
                podeDisponibilizarEmBloco(subprocesso, usuario, usarLocalizacoesPrecarregadas, localizacoesPrecarregadas)
        );
    }

    private SubprocessoElegivelDto toElegivelDto(Subprocesso sp, Unidade localizacao, ElegibilidadeAcaoBloco elegibilidade) {
        return SubprocessoElegivelDto.builder()
                .codigo(sp.getCodigo())
                .unidadeCodigo(sp.getUnidade().getCodigo())
                .unidadeNome(sp.getUnidade().getNome())
                .unidadeSigla(sp.getUnidade().getSigla())
                .localizacaoCodigo(localizacao.getCodigo())
                .situacao(sp.getSituacao())
                .habilitarAceitarCadastroBloco(elegibilidade.habilitarAceitarCadastroBloco())
                .habilitarAceitarMapaBloco(elegibilidade.habilitarAceitarMapaBloco())
                .habilitarHomologarCadastroBloco(elegibilidade.habilitarHomologarCadastroBloco())
                .habilitarHomologarMapaBloco(elegibilidade.habilitarHomologarMapaBloco())
                .habilitarDisponibilizarMapaBloco(elegibilidade.habilitarDisponibilizarMapaBloco())
                .ultimaDataLimite(obterUltimaDataLimite(sp))
                .build();
    }

    private List<ProcessoDetalheDto.AcaoBlocoDto> montarAcoesBloco(
            Processo processo,
            List<SubprocessoElegivelDto> subprocessosElegiveis,
            Perfil perfil
    ) {
        boolean processoAtivo = processo.getSituacao() != FINALIZADO;

        return List.of(
                criarAcaoBloco(AcaoBlocoContexto.builder()
                        .codigo("aceitar-cadastro")
                        .acao(ACEITAR)
                        .unidades(filtrarElegiveis(subprocessosElegiveis, SubprocessoElegivelDto::isHabilitarAceitarCadastroBloco))
                        .perfilPermite(AcaoPermissao.ACEITAR_CADASTRO_EM_BLOCO.permitePerfil(perfil))
                        .requerDataLimite(false)
                        .redirecionarPainel(true)
                        .rotulo(Mensagens.LABEL_ACEITAR_CADASTRO_BLOCO)
                        .titulo(Mensagens.TITULO_ACEITE_CADASTRO_BLOCO)
                        .texto(Mensagens.TEXTO_SELECAO_ACEITE_CADASTRO)
                        .rotuloBotao(Mensagens.BOTAO_REGISTRAR_ACEITE)
                        .mensagemSucesso(Mensagens.SUCESSO_ACEITE_CADASTRO_BLOCO)
                        .processoAtivo(processoAtivo)
                        .build()),

                criarAcaoBloco(AcaoBlocoContexto.builder()
                        .codigo("aceitar-mapa")
                        .acao(ACEITAR)
                        .unidades(filtrarElegiveis(subprocessosElegiveis, SubprocessoElegivelDto::isHabilitarAceitarMapaBloco))
                        .perfilPermite(AcaoPermissao.ACEITAR_MAPA_EM_BLOCO.permitePerfil(perfil))
                        .requerDataLimite(false)
                        .redirecionarPainel(true)
                        .rotulo(Mensagens.LABEL_ACEITAR_MAPA_BLOCO)
                        .titulo(Mensagens.TITULO_ACEITE_MAPA_BLOCO)
                        .texto(Mensagens.TEXTO_SELECAO_ACEITE_MAPA)
                        .rotuloBotao(Mensagens.BOTAO_REGISTRAR_ACEITE)
                        .mensagemSucesso(Mensagens.SUCESSO_ACEITE_MAPA_BLOCO)
                        .processoAtivo(processoAtivo)
                        .build()),

                criarAcaoBloco(AcaoBlocoContexto.builder()
                        .codigo("homologar-cadastro")
                        .acao(HOMOLOGAR)
                        .unidades(filtrarElegiveis(subprocessosElegiveis, SubprocessoElegivelDto::isHabilitarHomologarCadastroBloco))
                        .perfilPermite(AcaoPermissao.HOMOLOGAR_CADASTRO_EM_BLOCO.permitePerfil(perfil))
                        .requerDataLimite(false)
                        .redirecionarPainel(false)
                        .rotulo(Mensagens.LABEL_HOMOLOGAR_EM_BLOCO)
                        .titulo(Mensagens.TITULO_HOMOLOGACAO_CADASTRO_BLOCO)
                        .texto(Mensagens.TEXTO_SELECAO_HOMOLOGACAO_CADASTRO)
                        .rotuloBotao(Mensagens.BOTAO_HOMOLOGAR)
                        .mensagemSucesso(Mensagens.SUCESSO_HOMOLOGACAO_CADASTRO_BLOCO)
                        .processoAtivo(processoAtivo)
                        .build()),

                criarAcaoBloco(AcaoBlocoContexto.builder()
                        .codigo("homologar-mapa")
                        .acao(HOMOLOGAR)
                        .unidades(filtrarElegiveis(subprocessosElegiveis, SubprocessoElegivelDto::isHabilitarHomologarMapaBloco))
                        .perfilPermite(AcaoPermissao.HOMOLOGAR_MAPA_EM_BLOCO.permitePerfil(perfil))
                        .requerDataLimite(false)
                        .redirecionarPainel(true)
                        .rotulo(Mensagens.LABEL_HOMOLOGAR_MAPAS_BLOCO)
                        .titulo(Mensagens.TITULO_HOMOLOGACAO_MAPA_BLOCO)
                        .texto(Mensagens.TEXTO_SELECAO_HOMOLOGACAO_MAPA)
                        .rotuloBotao(Mensagens.BOTAO_HOMOLOGAR)
                        .mensagemSucesso(Mensagens.SUCESSO_HOMOLOGACAO_MAPA_BLOCO)
                        .processoAtivo(processoAtivo)
                        .build()),

                criarAcaoBloco(AcaoBlocoContexto.builder()
                        .codigo("disponibilizar-mapa")
                        .acao(DISPONIBILIZAR)
                        .unidades(filtrarElegiveis(subprocessosElegiveis, SubprocessoElegivelDto::isHabilitarDisponibilizarMapaBloco))
                        .perfilPermite(AcaoPermissao.DISPONIBILIZAR_MAPA_EM_BLOCO.permitePerfil(perfil))
                        .requerDataLimite(true)
                        .redirecionarPainel(true)
                        .rotulo(Mensagens.LABEL_DISPONIBILIZAR_EM_BLOCO)
                        .titulo(Mensagens.TITULO_DISPONIBILIZACAO_MAPA_BLOCO)
                        .texto(Mensagens.TEXTO_SELECAO_DISPONIBILIZACAO_MAPA)
                        .rotuloBotao(Mensagens.BOTAO_DISPONIBILIZAR)
                        .mensagemSucesso(Mensagens.SUCESSO_DISPONIBILIZACAO_MAPA_BLOCO)
                        .processoAtivo(processoAtivo)
                        .build())
        );
    }

    private ProcessoDetalheDto.AcaoBlocoDto criarAcaoBloco(AcaoBlocoContexto contexto) {
        boolean temUnidades = !contexto.unidades().isEmpty();
        boolean habilitar = contexto.perfilPermite() && contexto.processoAtivo() && temUnidades;
        return ProcessoDetalheDto.AcaoBlocoDto.builder()
                .codigo(contexto.codigo())
                .acao(contexto.acao())
                .mostrar(contexto.perfilPermite())
                .habilitar(habilitar)
                .requerDataLimite(contexto.requerDataLimite())
                .redirecionarPainel(contexto.redirecionarPainel())
                .rotulo(contexto.rotulo())
                .titulo(contexto.titulo())
                .texto(contexto.texto())
                .rotuloBotao(contexto.rotuloBotao())
                .mensagemSucesso(contexto.mensagemSucesso())
                .unidades(new ArrayList<>(contexto.unidades()))
                .build();
    }

    private List<SubprocessoElegivelDto> filtrarElegiveis(
            List<SubprocessoElegivelDto> subprocessosElegiveis,
            java.util.function.Predicate<SubprocessoElegivelDto> filtro
    ) {
        return subprocessosElegiveis.stream().filter(filtro).toList();
    }

    private boolean isSituacaoCadastroDisponibilizado(SituacaoSubprocesso situacao) {
        return situacao == MAPEAMENTO_CADASTRO_DISPONIBILIZADO || situacao == REVISAO_CADASTRO_DISPONIBILIZADA;
    }

    private boolean isSituacaoMapaAceitavel(SituacaoSubprocesso situacao) {
        return situacao == MAPEAMENTO_MAPA_COM_SUGESTOES
                || situacao == MAPEAMENTO_MAPA_VALIDADO
                || situacao == REVISAO_MAPA_COM_SUGESTOES
                || situacao == REVISAO_MAPA_VALIDADO;
    }

    private boolean isSituacaoMapaHomologavel(SituacaoSubprocesso situacao) {
        return situacao == MAPEAMENTO_MAPA_VALIDADO
                || situacao == REVISAO_MAPA_VALIDADO;
    }

    private UnidadeMapa obterUnidadeMapaObrigatorio(Map<Long, UnidadeMapa> mapasPorUnidade, Long codigoUnidade) {
        UnidadeMapa unidadeMapa = mapasPorUnidade.get(codigoUnidade);
        if (unidadeMapa == null) {
            throw new IllegalStateException("Unidade %d sem mapa vigente para iniciar subprocesso".formatted(codigoUnidade));
        }
        return unidadeMapa;
    }

    private Unidade obterUnidadeObrigatoria(Map<Long, Unidade> unidadesPorCodigo, Long codigoUnidade) {
        Unidade unidade = unidadesPorCodigo.get(codigoUnidade);
        if (unidade == null) {
            throw new IllegalStateException("Unidade %d ausente para iniciar subprocesso".formatted(codigoUnidade));
        }
        return unidade;
    }

    private Map<Long, Unidade> carregarUnidadesPorCodigo(List<Long> codigosUnidades) {
        return unidadeService.buscarPorCodigos(codigosUnidades).stream()
                .collect(Collectors.toMap(Unidade::getCodigo, unidade -> unidade, (primeira, duplicada) -> primeira));
    }

    private @Nullable LocalDateTime obterUltimaDataLimite(Subprocesso sp) {
        LocalDateTime dataLimiteEtapa1 = sp.getDataLimiteEtapa1();
        LocalDateTime dataLimiteEtapa2 = sp.getDataLimiteEtapa2();

        if (dataLimiteEtapa1 == null && dataLimiteEtapa2 == null) {
            return null;
        }
        if (dataLimiteEtapa1 == null) {
            throw new IllegalStateException("Subprocesso %d com data limite da etapa 2 sem data limite da etapa 1"
                    .formatted(sp.getCodigo()));
        }
        return Objects.requireNonNullElse(dataLimiteEtapa2, dataLimiteEtapa1);
    }

    private void criarAlertasInicioProcesso(Processo processo, List<Unidade> participantes) {
        log.info("Criando alertas de início do processo {}", processo.getCodigo());
        servicoAlertas.criarAlertasProcessoIniciado(processo, participantes);
    }

    private void criarNotificacoesInicioProcesso(Processo processo, List<Unidade> participantes) {
        List<Subprocesso> subprocessos = Objects.requireNonNullElse(
                consultaService.listarEntidadesPorProcesso(processo.getCodigo()),
                List.of()
        );
        Map<Long, Subprocesso> subprocessoPorUnidade = subprocessos
                .stream()
                .collect(Collectors.toMap(
                        subprocesso -> subprocesso.getUnidade().getCodigo(),
                        subprocesso -> subprocesso,
                        (atual, ignorar) -> atual
                ));

        Set<Long> codsOperacionais = new HashSet<>();
        Set<Long> codsIntermediarias = new HashSet<>();
        Map<Long, Unidade> todasUnidadesMap = new HashMap<>();

        for (Unidade unidade : participantes) {
            Long codigoUnidade = unidade.getCodigo();
            todasUnidadesMap.put(codigoUnidade, unidade);
            TipoUnidade tipo = unidade.getTipo();

            if (tipo == TipoUnidade.OPERACIONAL || tipo == TipoUnidade.INTEROPERACIONAL || tipo == TipoUnidade.RAIZ) {
                codsOperacionais.add(codigoUnidade);
            }

            if (tipo == TipoUnidade.INTERMEDIARIA || tipo == TipoUnidade.INTEROPERACIONAL) {
                codsIntermediarias.add(codigoUnidade);
            }
        }

        Set<Long> codigosSuperiores = new HashSet<>();
        for (Unidade unidade : participantes) {
            codigosSuperiores.addAll(unidadeHierarquiaService.buscarCodigosSuperiores(unidade.getCodigo()));
        }
        if (!codigosSuperiores.isEmpty()) {
            List<Unidade> unidadesSuperiores = unidadeService.buscarPorCodigos(new ArrayList<>(codigosSuperiores));
            unidadesSuperiores.forEach(u -> {
                todasUnidadesMap.put(u.getCodigo(), u);
                codsIntermediarias.add(u.getCodigo());
            });
        }

        Map<Long, List<String>> subordinadasPorSuperior = mapearSiglasSubordinadasPorSuperior(participantes);
        LocalDateTime dataLimite = obterDataLimiteObrigatoria(processo, processo.getCodigo());

        for (Long cod : codsOperacionais) {
            Unidade unidadeDestino = obterUnidadeObrigatoria(todasUnidadesMap, cod);
            criarNotificacaoInicio(
                    processo,
                    unidadeDestino,
                    true,
                    subordinadasPorSuperior,
                    dataLimite,
                    subprocessoPorUnidade.get(unidadeDestino.getCodigo())
            );
        }

        for (Long cod : codsIntermediarias) {
            // Se já foi notificada como operacional, não notifica como intermediária
            if (codsOperacionais.contains(cod)) continue;
            Unidade unidadeDestino = obterUnidadeObrigatoria(todasUnidadesMap, cod);
            criarNotificacaoInicio(
                    processo,
                    unidadeDestino,
                    false,
                    subordinadasPorSuperior,
                    dataLimite,
                    subprocessoPorUnidade.get(unidadeDestino.getCodigo())
            );
        }
    }

    private void criarNotificacaoInicio(Processo processo, Unidade unidadeDestino, boolean participante,
                                        Map<Long, List<String>> subordinadasPorSuperior, LocalDateTime dataLimite,
                                        @Nullable Subprocesso subprocessoDestino) {
        List<String> subordinadas = subordinadasPorSuperior.getOrDefault(unidadeDestino.getCodigo(), List.of());
        String corpoHtml = emailModelosService.criarEmailInicioProcessoConsolidado(
                unidadeDestino.getSigla(),
                processo.getDescricao(),
                dataLimite,
                processo.getTipo().name(),
                participante,
                subordinadas
        );
        String assunto = emailModelosService.criarAssuntoInicioProcesso(
                processo.getTipo().name(),
                participante
        );

        enfileirarNotificacaoUnidade(
                unidadeDestino,
                TipoNotificacao.PROCESSO_INICIADO,
                assunto,
                corpoHtml,
                chaveInicioProcesso(processo, unidadeDestino, participante),
                subprocessoDestino
        );
    }

    private Map<Long, List<String>> mapearSiglasSubordinadasPorSuperior(List<Unidade> participantes) {
        Map<Long, List<String>> subordinadasPorSuperior = new HashMap<>();
        for (Unidade participante : participantes) {
            for (Long codigoSuperior : unidadeHierarquiaService.buscarCodigosSuperiores(participante.getCodigo())) {
                subordinadasPorSuperior
                        .computeIfAbsent(codigoSuperior, ignored -> new ArrayList<>())
                        .add(participante.getSigla());
            }
        }
        subordinadasPorSuperior.values().forEach(Collections::sort);
        return subordinadasPorSuperior;
    }

    private String emailUnidade(Unidade unidade) {
        return "%s@tre-pe.jus.br".formatted(unidade.getSigla().toLowerCase(Locale.ROOT));
    }

    private void enfileirarNotificacaoUnidade(
            Unidade unidadeDestino,
            TipoNotificacao tipoNotificacao,
            String assunto,
            String corpoHtml,
            String chaveIdempotencia,
            @Nullable Subprocesso subprocesso
    ) {
        String destinatarioPrincipal = emailDestinoPrincipal(unidadeDestino);

        notificacaoService.enfileirar(EnfileirarNotificacaoCommand.builder()
                .subprocesso(subprocesso)
                .tipoNotificacao(tipoNotificacao)
                .unidadeDestinoSigla(unidadeDestino.getSigla())
                .destinatario(destinatarioPrincipal)
                .assunto(assunto)
                .corpoHtml(corpoHtml)
                .chaveIdempotencia(chaveIdempotencia)
                .build());

        Optional<String> destinatarioCopia = emailCopiaAdmin(unidadeDestino);
        if (destinatarioCopia.isPresent()) {
            notificacaoService.enfileirar(EnfileirarNotificacaoCommand.builder()
                    .subprocesso(subprocesso)
                    .tipoNotificacao(tipoNotificacao)
                    .unidadeDestinoSigla(unidadeDestino.getSigla())
                    .destinatario(destinatarioCopia.get())
                    .assunto(assunto)
                    .corpoHtml(corpoHtml)
                    .chaveIdempotencia(chaveIdempotencia + ":copia-admin")
                    .build());
        }
    }

    private String emailDestinoPrincipal(Unidade unidadeDestino) {
        if (isUnidadeAdmin(unidadeDestino)) {
            return "%s@tre-pe.jus.br".formatted(SIGLA_UNIDADE_SEDOC.toLowerCase(Locale.ROOT));
        }
        return emailUnidade(unidadeDestino);
    }

    private Optional<String> emailCopiaAdmin(Unidade unidadeDestino) {
        if (!isUnidadeAdmin(unidadeDestino)) {
            return Optional.empty();
        }

        // Busca o usuário com unidadeLotacao inicializada via JOIN FETCH para evitar
        // LazyInitializationException ao acessar getSigla() fora de sessão Hibernate.
        Usuario usuarioPrincipal = usuarioService.usuarioAutenticado();
        Usuario usuario = usuarioService.buscarUsuarioComUnidadeLotacao(usuarioPrincipal.getTituloEleitoral());
        Unidade unidadeLotacao = usuario.getUnidadeLotacao();
        if (unidadeLotacao != null && SIGLA_UNIDADE_SEDOC.equalsIgnoreCase(unidadeLotacao.getSigla())) {
            return Optional.empty();
        }

        return Optional.of(usuario.getEmail());
    }

    private boolean isUnidadeAdmin(Unidade unidadeDestino) {
        return SIGLA_UNIDADE_ADMIN.equalsIgnoreCase(unidadeDestino.getSigla());
    }

    private String chaveInicioProcesso(Processo processo, Unidade unidadeDestino, boolean participante) {
        String sufixo = participante ? "direto" : "subordinada";
        return "processo:%d:inicio:unidade:%d:%s".formatted(
                processo.getCodigo(),
                unidadeDestino.getCodigo(),
                sufixo
        );
    }

    private void criarAlertaLembretePrazo(Processo processo, Unidade unidade, String dataLimiteText) {
        servicoAlertas.criarAlertaAdmin(processo, unidade,
                "Lembrete: Prazo do processo " + processo.getDescricao() + " encerra em " + dataLimiteText);
    }

    private void criarAlertasFinalizacaoProcesso(Processo p) {
        log.info("Criando alertas de finalização do processo {}", p.getCodigo());
        List<Long> unidadesCods = p.getParticipantes().stream().map(UnidadeProcesso::getUnidadeCodigoPersistido).toList();
        List<Unidade> participantes = unidadeService.buscarPorCodigos(unidadesCods);
        for (Unidade u : participantes) {
            servicoAlertas.criarAlertaAdmin(p, u, "Processo finalizado: " + p.getDescricao());
        }
    }

    private void criarNotificacoesFinalizacaoProcesso(Processo processo) {
        log.info("Criando notificações de finalização do processo {}", processo.getCodigo());
        List<Unidade> participantes = buscarParticipantes(processo);
        Set<Long> codigosDiretos = new HashSet<>();
        Set<Long> codigosConsolidados = new HashSet<>();
        Map<Long, Unidade> unidades = new HashMap<>();

        for (Unidade participante : participantes) {
            unidades.put(participante.getCodigo(), participante);
            TipoUnidade tipo = participante.getTipo();
            if (tipo == TipoUnidade.OPERACIONAL || tipo == TipoUnidade.INTEROPERACIONAL) {
                codigosDiretos.add(participante.getCodigo());
            }
            if (tipo == TipoUnidade.INTERMEDIARIA || tipo == TipoUnidade.INTEROPERACIONAL) {
                codigosConsolidados.add(participante.getCodigo());
            }
        }

        Map<Long, List<String>> subordinadasPorSuperior = mapearSiglasSubordinadasPorSuperior(participantes);
        carregarUnidadesConsolidadas(codigosConsolidados, subordinadasPorSuperior, unidades);

        for (Long codigoUnidade : codigosDiretos) {
            criarNotificacaoFinalizacaoDireta(processo, obterUnidadeObrigatoria(unidades, codigoUnidade));
        }
        for (Long codigoUnidade : codigosConsolidados) {
            List<String> subordinadas = subordinadasPorSuperior.getOrDefault(codigoUnidade, List.of());
            if (subordinadas.isEmpty()) continue;
            criarNotificacaoFinalizacaoConsolidada(processo, obterUnidadeObrigatoria(unidades, codigoUnidade), subordinadas);
        }
    }

    private List<Unidade> buscarParticipantes(Processo processo) {
        List<Long> codigosUnidades = processo.getParticipantes().stream()
                .map(UnidadeProcesso::getUnidadeCodigoPersistido)
                .toList();
        return unidadeService.buscarPorCodigos(codigosUnidades);
    }

    private void carregarUnidadesConsolidadas(
            Set<Long> codigosConsolidados,
            Map<Long, List<String>> subordinadasPorSuperior,
            Map<Long, Unidade> unidades
    ) {
        codigosConsolidados.addAll(subordinadasPorSuperior.keySet());
        List<Long> codigosFaltantes = codigosConsolidados.stream()
                .filter(codigo -> !unidades.containsKey(codigo))
                .toList();
        if (codigosFaltantes.isEmpty()) return;

        unidadeService.buscarPorCodigos(codigosFaltantes)
                .forEach(unidade -> unidades.put(unidade.getCodigo(), unidade));
    }

    private void criarNotificacaoFinalizacaoDireta(Processo processo, Unidade unidade) {
        String corpo = emailModelosService.criarEmailProcessoFinalizadoPorUnidade(
                unidade.getSigla(),
                processo.getDescricao()
        );
        enfileirarNotificacaoUnidade(
                unidade,
                TipoNotificacao.PROCESSO_FINALIZADO,
                "SGC: Finalização do processo " + processo.getDescricao(),
                corpo,
                chaveFinalizacaoProcesso(processo, unidade, true),
                null
        );
    }

    private void criarNotificacaoFinalizacaoConsolidada(Processo processo, Unidade unidade, List<String> subordinadas) {
        String corpo = emailModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(
                unidade.getSigla(),
                processo.getDescricao(),
                subordinadas
        );
        enfileirarNotificacaoUnidade(
                unidade,
                TipoNotificacao.PROCESSO_FINALIZADO,
                "SGC: Finalização do processo " + processo.getDescricao() + " em unidades subordinadas",
                corpo,
                chaveFinalizacaoProcesso(processo, unidade, false),
                null
        );
    }

    private String chaveFinalizacaoProcesso(Processo processo, Unidade unidade, boolean direto) {
        String sufixo = direto ? "direto" : "subordinada";
        return "processo:%d:finalizacao:unidade:%d:%s".formatted(
                processo.getCodigo(),
                unidade.getCodigo(),
                sufixo
        );
    }

    private void executarDisponibilizacaoMapaEmBloco(
            DisponibilizarMapaEmBlocoCommand command,
            Usuario usuario,
            List<Subprocesso> subprocessos
    ) {
        if (!permissionEvaluator.verificarPermissao(usuario, subprocessos, DISPONIBILIZAR_MAPA)) {
            throw new ErroAcessoNegado(Mensagens.SEM_PERMISSAO_DISPONIBILIZAR);
        }
        DisponibilizarMapaRequest dispReq = new DisponibilizarMapaRequest(command.dataLimite(), "Disponibilização em bloco");
        transicaoService.disponibilizarMapaEmBloco(subprocessos, dispReq, usuario);
    }

    private void processarAcoesBlocoAceiteHomologacao(ProcessarAnaliseEmBlocoCommand req, List<Subprocesso> list) {
        Map<Boolean, List<Long>> separacao = list.stream()
                .collect(Collectors.partitioningBy(
                        sp -> isSituacaoCadastro(sp.getSituacao()),
                        Collectors.mapping(Subprocesso::getCodigo, Collectors.toList())
                ));

        List<Long> cadastro = separacao.getOrDefault(true, List.of());
        List<Long> validacao = separacao.getOrDefault(false, List.of());

        switch (req.acao()) {
            case ACEITAR -> executarTransicoesEmBloco(
                    cadastro,
                    validacao,
                    cadastroFluxoService::aceitarCadastroEmBloco,
                    transicaoService::aceitarValidacaoEmBloco
            );
            case HOMOLOGAR -> executarTransicoesEmBloco(
                    cadastro,
                    validacao,
                    cadastroFluxoService::homologarCadastroEmBloco,
                    transicaoService::homologarValidacaoEmBloco
            );
            default -> log.debug("Ação em bloco {} sem processamento no fluxo de análise", req.acao());
        }
    }

    private void executarTransicoesEmBloco(
            List<Long> codigosCadastro,
            List<Long> codigosValidacao,
            java.util.function.Consumer<List<Long>> acaoCadastro,
            java.util.function.Consumer<List<Long>> acaoValidacao
    ) {
        if (!codigosCadastro.isEmpty()) {
            acaoCadastro.accept(codigosCadastro);
        }
        if (!codigosValidacao.isEmpty()) {
            acaoValidacao.accept(codigosValidacao);
        }
    }

    private boolean isSituacaoCadastro(SituacaoSubprocesso s) {
        return s == MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
                s == REVISAO_CADASTRO_DISPONIBILIZADA ||
                s == REVISAO_CADASTRO_HOMOLOGADA;
    }

    // Check permission helper
    public boolean checarAcesso(@Nullable Authentication auth, Long cod) {
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Usuario usuario)) return false;
        if (usuario.getPerfilAtivo() == Perfil.ADMIN) return true;

        List<Long> unidadesAcesso = buscarCodigosAcesso(usuario);
        return !consultaService.listarEntidadesPorProcessoEUnidades(cod, unidadesAcesso).isEmpty();
    }

    private void validarSelecaoBloco(List<Long> codigos, List<Subprocesso> list) {
        if (codigos.size() != list.size()) {
            Set<Long> encontrados = list.stream().map(Subprocesso::getUnidade).map(Unidade::getCodigo).collect(Collectors.toSet());
            List<Long> faltando = codigos.stream().filter(codigo -> !encontrados.contains(codigo)).toList();
            throw new ErroValidacao(Mensagens.UNIDADES_SEM_SUBPROCESSOS.formatted(faltando));
        }
    }

    private ContextoInicioProcesso resolverContextoInicio(Processo processo, List<Long> codsUnidadesParam) {
        TipoProcesso tipo = processo.getTipo();
        List<Long> codigosUnidades = tipo == REVISAO
                ? validarCodigosRevisao(codsUnidadesParam)
                : validarCodigosParticipantes(processo.getCodigosParticipantes());
        Set<Unidade> unidadesParaProcessar = new HashSet<>(unidadeService.buscarPorCodigos(codigosUnidades));

        if (tipo == REVISAO) {
            processo.sincronizarParticipantes(carregarArvoreUnidades(unidadesParaProcessar));
        }

        return new ContextoInicioProcesso(tipo, codigosUnidades, unidadesParaProcessar);
    }

    private List<Long> validarCodigosRevisao(List<Long> codsUnidadesParam) {
        if (codsUnidadesParam.isEmpty()) {
            throw new ErroValidacao(Mensagens.LISTA_UNIDADES_OBRIGATORIA_REVISAO);
        }
        Set<Long> codigosInteroperacionaisSelecionados = unidadeService.buscarPorCodigos(codsUnidadesParam).stream()
                .filter(unidade -> unidade.getTipo() == TipoUnidade.INTEROPERACIONAL)
                .map(Unidade::getCodigo)
                .collect(Collectors.toSet());

        Map<Long, Long> mapaFilhoPai = unidadeHierarquiaService.buscarMapaFilhoPai();
        return removerAncestraisRedundantesRevisao(codsUnidadesParam, mapaFilhoPai, codigosInteroperacionaisSelecionados);
    }

    private List<Long> removerAncestraisRedundantesRevisao(
            List<Long> codigosSelecionados,
            Map<Long, Long> mapaFilhoPai,
            Set<Long> codigosInteroperacionaisSelecionados
    ) {
        LinkedHashSet<Long> codigosUnicos = new LinkedHashSet<>(codigosSelecionados);
        Set<Long> codigosParaRemover = new HashSet<>();

        for (Long codigoSelecionado : codigosUnicos) {
            // Navega pelo mapa filho->pai já carregado para evitar múltiplas leituras de hierarquia.
            Long codigoSuperior = mapaFilhoPai.get(codigoSelecionado);
            while (codigoSuperior != null) {
                if (codigosUnicos.contains(codigoSuperior)
                        && !codigosInteroperacionaisSelecionados.contains(codigoSuperior)) {
                    codigosParaRemover.add(codigoSuperior);
                }
                codigoSuperior = mapaFilhoPai.get(codigoSuperior);
            }
        }

        return codigosUnicos.stream()
                .filter(codigo -> !codigosParaRemover.contains(codigo))
                .toList();
    }

    private List<Long> validarCodigosParticipantes(List<Long> codigosParticipantes) {
        if (codigosParticipantes.isEmpty()) {
            throw new ErroValidacao(Mensagens.SEM_UNIDADES_PARTICIPANTES);
        }
        return codigosParticipantes;
    }

    private LocalDateTime obterDataLimiteObrigatoria(Processo processo, Long codProcesso) {
        LocalDateTime dataLimite = processo.getDataLimite();
        if (dataLimite == null) {
            throw new IllegalStateException("Processo %d sem data limite para envio de lembrete".formatted(codProcesso));
        }
        return dataLimite;
    }

    private record ElegibilidadeAcaoBloco(
            boolean habilitarAceitarCadastroBloco,
            boolean habilitarAceitarMapaBloco,
            boolean habilitarHomologarCadastroBloco,
            boolean habilitarHomologarMapaBloco,
            boolean habilitarDisponibilizarMapaBloco
    ) {
        private boolean possuiAlgumaAcao() {
            return habilitarAceitarCadastroBloco
                    || habilitarAceitarMapaBloco
                    || habilitarHomologarCadastroBloco
                    || habilitarHomologarMapaBloco
                    || habilitarDisponibilizarMapaBloco;
        }
    }

    record ContextoInicioProcesso(
            TipoProcesso tipo,
            List<Long> codigosUnidades,
            Set<Unidade> unidadesParaProcessar
    ) {
    }

    record InicioSubprocessosContexto(
            Processo processo,
            TipoProcesso tipo,
            List<Long> codigosUnidades,
            Set<Unidade> unidadesParaProcessar,
            List<UnidadeMapa> unidadesMapas,
            Unidade unidadeAdmin
    ) {
    }

    @Builder
    private record AcaoBlocoContexto(
            String codigo,
            AcaoProcesso acao,
            List<SubprocessoElegivelDto> unidades,
            boolean perfilPermite,
            boolean requerDataLimite,
            boolean redirecionarPainel,
            String rotulo,
            String titulo,
            String texto,
            String rotuloBotao,
            String mensagemSucesso,
            boolean processoAtivo
    ) {
    }
}

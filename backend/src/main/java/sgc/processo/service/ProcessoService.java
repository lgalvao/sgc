package sgc.processo.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaFacade;
import sgc.comum.MsgValidacao;
import sgc.comum.erros.*;
import sgc.comum.model.ComumRepo;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.alerta.EmailService;
import sgc.alerta.EmailModelosService;
import sgc.organizacao.UsuarioFacade;
import sgc.processo.dto.*;
import sgc.processo.dto.ProcessoDetalheDto.UnidadeParticipanteDto;
import sgc.processo.model.*;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private final ProcessoRepo processoRepo;
    private final ComumRepo repo;
    private final UnidadeService unidadeService;
    private final SubprocessoService subprocessoService;
    private final SubprocessoValidacaoService validacaoService;
    private final UsuarioFacade usuarioService;
    private final AlertaFacade servicoAlertas;
    private final EmailService emailService;
    private final EmailModelosService emailModelosService;
    private final SgcPermissionEvaluator permissionEvaluator;
    private final SubprocessoTransicaoService transicaoService;


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
        return Optional.ofNullable(repo.buscar(Processo.class, codigo));
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
        return processoRepo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Processo> listarIniciadosPorParticipantes(List<Long> unidadeCodigos, Pageable pageable) {
        return processoRepo.listarPorParticipantesESituacaoDiferente(
                unidadeCodigos, CRIADO, pageable);
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
        Long codUnidadeUsuario = usuario.getUnidadeAtivaCodigo();

        List<Subprocesso> subprocessos;
        if (usuario.getPerfilAtivo() == Perfil.ADMIN) {
            subprocessos = subprocessoService.listarEntidadesPorProcesso(codProcesso);
        } else {
            List<Long> unidadesAcesso = buscarCodigosAcesso(usuario);
            subprocessos = subprocessoService.listarEntidadesPorProcessoEUnidades(codProcesso, unidadesAcesso);
        }

        return subprocessos.stream()
                .filter(sp -> isElegivelParaAcaoEmBloco(sp, usuario, codUnidadeUsuario))
                .map(this::toElegivelDto)
                .toList();
    }


    public Processo criar(CriarProcessoRequest req) {
        List<Long> codigosUnidades = new ArrayList<>(req.unidades());
        validarUnidadesParaProcesso(req.tipo(), codigosUnidades);

        Set<Unidade> participantes = codigosUnidades.stream()
                .map(unidadeService::buscarPorCodigo)
                .collect(Collectors.toSet());

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
            throw new ErroValidacao(MsgValidacao.PROCESSO_SO_EDITAVEL_EM_CRIADO);
        }

        validarUnidadesParaProcesso(req.tipo(), new ArrayList<>(req.unidades()));

        Set<Unidade> participantes = req.unidades().stream()
                .map(unidadeService::buscarPorCodigo)
                .collect(Collectors.toSet());

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
            throw new ErroValidacao(MsgValidacao.PROCESSO_SO_REMOVIVEL_EM_CRIADO);
        }
        processoRepo.deleteById(codigo);
        log.info("Processo {} removido.", codigo);
    }


    public List<String> iniciar(Long codigo, List<Long> codsUnidadesParam, Usuario usuario) {
        Processo processo = buscarPorCodigo(codigo);
        if (processo.getSituacao() != CRIADO) {
            throw new ErroValidacao(MsgValidacao.PROCESSO_SO_INICIAVEL_EM_CRIADO);
        }

        TipoProcesso tipo = processo.getTipo();
        List<Long> codigosUnidades;
        Set<Unidade> unidadesParaProcessar;

        if (tipo == REVISAO) {
            if (codsUnidadesParam.isEmpty()) {
                throw new ErroValidacao(MsgValidacao.LISTA_UNIDADES_OBRIGATORIA_REVISAO);
            }
            codigosUnidades = codsUnidadesParam;
            unidadesParaProcessar = new HashSet<>(unidadeService.porCodigos(codigosUnidades));
            processo.sincronizarParticipantes(carregarArvoreUnidades(unidadesParaProcessar));
        } else {
            codigosUnidades = processo.getCodigosParticipantes();
            if (codigosUnidades.isEmpty()) {
                throw new ErroValidacao(MsgValidacao.SEM_UNIDADES_PARTICIPANTES);
            }
            unidadesParaProcessar = new HashSet<>(unidadeService.porCodigos(codigosUnidades));
        }

        List<String> erros = validarUnidadesInicio(tipo, codigosUnidades);
        if (!erros.isEmpty()) return erros;

        List<UnidadeMapa> unidadesMapas = (tipo == REVISAO || tipo == DIAGNOSTICO)
                ? unidadeService.buscarMapasPorUnidades(codigosUnidades)
                : List.of();

        Unidade admin = repo.buscarPorSigla(Unidade.class, "ADMIN");
        efetivarInicioSubprocessos(processo, tipo, codigosUnidades, unidadesParaProcessar, unidadesMapas, admin, usuario);
        
        processo.setSituacao(EM_ANDAMENTO);
        processoRepo.save(processo);
        notificarInicioProcesso(processo, new ArrayList<>(unidadesParaProcessar));

        log.info("Processo {} iniciado para {} unidades.", codigo, codigosUnidades.size());
        return List.of();
    }

    public void finalizar(Long codigo) {
        Processo processo = buscarPorCodigo(codigo);
        validarFinalizacao(processo);

        if (processo.getTipo() != DIAGNOSTICO) {
            tornarMapasVigentes(processo);
        }
        notificarFinalizacaoProcesso(processo);

        processo.setSituacao(FINALIZADO);
        processo.setDataFinalizacao(LocalDateTime.now());
        processoRepo.save(processo);


        log.info("Processo {} finalizado", codigo);
    }


    public void executarAcaoEmBloco(Long codProcesso, AcaoEmBlocoRequest req) {
        Usuario usuario = usuarioService.usuarioAutenticado();
        List<Subprocesso> subprocessos = subprocessoService.listarEntidadesPorProcessoEUnidades(codProcesso, req.unidadeCodigos());
        
        if (req.unidadeCodigos().isEmpty()) throw new ErroValidacao(MsgValidacao.SELECIONE_AO_MENOS_UMA_UNIDADE);
        validarSelecaoBloco(req.unidadeCodigos(), subprocessos);

        if (req.acao() == DISPONIBILIZAR) {
            if (!permissionEvaluator.verificarPermissao(usuario, subprocessos, DISPONIBILIZAR_MAPA)) {
                throw new ErroAcessoNegado(MsgValidacao.SEM_PERMISSAO_DISPONIBILIZAR);
            }
            DisponibilizarMapaRequest dispReq = new DisponibilizarMapaRequest(req.dataLimite(), "Disponibilização em bloco");
            transicaoService.disponibilizarMapaEmBloco(subprocessos.stream().map(Subprocesso::getCodigo).toList(), dispReq, usuario);
            return;
        }

        processarAcoesBlocoAceiteHomologacao(req, usuario, subprocessos);
    }


    @Transactional(readOnly = true)
    public ProcessoDetalheDto obterDetalhesCompleto(Long codProcesso, Usuario usuario, boolean incluirElegiveis) {
        Processo processo = buscarPorCodigo(codProcesso);
        List<Subprocesso> subprocessos = subprocessoService.listarEntidadesPorProcessoComUnidade(codProcesso);
        Set<Long> unidadesAcesso = obterIdsUnidadesAcesso(processo, usuario);

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
                .podeHomologarCadastro(permissionEvaluator.verificarPermissao(usuario, processo, HOMOLOGAR_CADASTRO_EM_BLOCO))
                .podeHomologarMapa(permissionEvaluator.verificarPermissao(usuario, processo, HOMOLOGAR_MAPA_EM_BLOCO))
                .podeAceitarCadastroBloco(permissionEvaluator.verificarPermissao(usuario, processo, ACEITAR_CADASTRO_EM_BLOCO))
                .podeDisponibilizarMapaBloco(permissionEvaluator.verificarPermissao(usuario, processo, DISPONIBILIZAR_MAPA_EM_BLOCO))
                .unidades(new ArrayList<>())
                .build();

        montarHierarquiaNoDto(dto, processo, subprocessos, unidadesAcesso);
        if (incluirElegiveis) {
            dto.getElegiveis().addAll(listarSubprocessosElegiveis(codProcesso));
        }

        return dto;
    }


    public void enviarLembrete(Long codProcesso, Long unidadeCodigo) {
        Processo processo = buscarPorCodigoComParticipantes(codProcesso);
        Unidade unidade = unidadeService.buscarPorCodigo(unidadeCodigo);

        if (processo.getParticipantes().stream().noneMatch(u -> u.getUnidadeCodigo().equals(unidadeCodigo))) {
            throw new ErroValidacao(MsgValidacao.UNIDADE_NAO_PARTICIPA);
        }

        String dataLimiteText = processo.getDataLimite() != null 
            ? processo.getDataLimite().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
        String corpoHtml = emailModelosService.criarEmailLembretePrazo(unidade.getSigla(), processo.getDescricao(), processo.getDataLimite());

        Subprocesso sp = subprocessoService.obterEntidadePorProcessoEUnidade(codProcesso, unidadeCodigo);
        subprocessoService.registrarMovimentacaoLembrete(sp.getCodigo());

        Usuario titular = usuarioService.buscarPorLogin(unidade.getTituloTitular());
        emailService.enviarEmailHtml(titular.getEmail(), "SGC: Lembrete - " + processo.getDescricao(), corpoHtml);
        servicoAlertas.criarAlertaAdmin(processo, unidade, "Lembrete: Prazo encerra em " + dataLimiteText);
    }


    private List<Long> buscarCodigosAcesso(Usuario usuario) {
        Long root = usuario.getUnidadeAtivaCodigo();
        if (usuario.getPerfilAtivo() == Perfil.GESTOR) {
            return buscarDescendentes(root);
        }
        return List.of(root);
    }

    private List<Long> buscarDescendentes(Long codRaiz) {
        List<Unidade> todas = unidadeService.todasComHierarquia();
        Map<Long, List<Unidade>> porPai = todas.stream()
                .filter(u -> u.getUnidadeSuperior() != null)
                .collect(Collectors.groupingBy(u -> u.getUnidadeSuperior().getCodigo()));
        
        Set<Long> result = new HashSet<>();
        Queue<Long> fila = new LinkedList<>();
        fila.add(codRaiz);
        result.add(codRaiz);
        while (!fila.isEmpty()) {
            Long atual = fila.poll();
            List<Unidade> filhos = porPai.get(atual);
            if (filhos != null) {
                for (Unidade f : filhos) {
                    if (result.add(f.getCodigo())) fila.add(f.getCodigo());
                }
            }
        }
        return new ArrayList<>(result);
    }

    private void validarUnidadesParaProcesso(TipoProcesso tipo, List<Long> codigosUnidades) {
        List<Unidade> entidades = codigosUnidades.stream().map(unidadeService::buscarPorCodigo).toList();
        List<String> invalidas = entidades.stream()
                .filter(u -> u.getTipo() == TipoUnidade.INTERMEDIARIA)
                .map(Unidade::getSigla).toList();
        if (!invalidas.isEmpty()) throw new ErroValidacao(MsgValidacao.UNIDADES_INTERMEDIARIA_INVALIDAS.formatted(String.join(", ", invalidas)));

        if (tipo == REVISAO || tipo == DIAGNOSTICO) {
            List<String> semMapa = codigosUnidades.stream()
                    .filter(codigo -> !unidadeService.verificarMapaVigente(codigo))
                    .map(codigo -> unidadeService.buscarPorCodigo(codigo).getSigla()).toList();
            if (!semMapa.isEmpty()) throw new ErroValidacao(MsgValidacao.UNIDADES_SEM_MAPA_VIGENTE.formatted(String.join(", ", semMapa)));
        }
    }

    private List<String> validarUnidadesInicio(TipoProcesso tipo, List<Long> cods) {
        List<String> erros = new ArrayList<>();
        if (tipo == REVISAO || tipo == DIAGNOSTICO) {
            unidadeService.buscarSiglasPorCodigos(cods.stream().filter(codigo -> !unidadeService.verificarMapaVigente(codigo)).toList())
                    .stream().findFirst().ifPresent(s -> erros.add(MsgValidacao.UNIDADES_SEM_MAPA));
        }
        List<Long> bloqueadas = processoRepo.listarUnidadesEmProcessoAtivo(EM_ANDAMENTO, cods);
        if (!bloqueadas.isEmpty()) erros.add(MsgValidacao.UNIDADES_EM_PROCESSO_ATIVO);
        return erros;
    }

    private void validarFinalizacao(Processo processo) {
        if (processo.getSituacao() != EM_ANDAMENTO) throw new ErroValidacao(MsgValidacao.SITUACAO_INVALIDA);
        if (!validacaoService.validarSubprocessosParaFinalizacao(processo.getCodigo()).valido()) 
            throw new ErroValidacao(MsgValidacao.SUBPROCESSOS_NAO_HOMOLOGADOS);
    }

    private void tornarMapasVigentes(Processo processo) {
        subprocessoService.listarEntidadesPorProcesso(processo.getCodigo())
                .forEach(sp -> unidadeService.definirMapaVigente(sp.getUnidade().getCodigo(), sp.getMapa()));
    }

    private Set<Unidade> carregarArvoreUnidades(Set<Unidade> participantes) {
        Set<Unidade> arvore = new HashSet<>(participantes);
        for (Unidade u : participantes) {
            Unidade sup = u.getUnidadeSuperior();
            while (sup != null) { arvore.add(sup); sup = sup.getUnidadeSuperior(); }
        }
        return arvore;
    }

    private void efetivarInicioSubprocessos(Processo processo, TipoProcesso tipo, List<Long> cods, Set<Unidade> pars, List<UnidadeMapa> ums, Unidade adm, Usuario user) {
        Map<Long, UnidadeMapa> mapUm = ums.stream().collect(Collectors.toMap(UnidadeMapa::getUnidadeCodigo, m -> m));
        if (tipo == MAPEAMENTO) subprocessoService.criarParaMapeamento(processo, pars, adm, user);
        else if (tipo == REVISAO) cods.forEach(c -> subprocessoService.criarParaRevisao(processo, unidadeService.buscarPorCodigo(c), mapUm.get(c), adm, user));
        else if (tipo == DIAGNOSTICO) pars.forEach(u -> subprocessoService.criarParaDiagnostico(processo, u, mapUm.get(u.getCodigo()), adm, user));
    }

    private void montarHierarquiaNoDto(ProcessoDetalheDto dto, Processo processo, List<Subprocesso> subps, Set<Long> acesso) {
        Map<Long, UnidadeParticipanteDto> mapDto = new HashMap<>();
        Map<Long, Subprocesso> mapSp = subps.stream().collect(Collectors.toMap(s -> s.getUnidade().getCodigo(), s -> s));

        processo.getParticipantes().stream()
                .filter(p -> acesso == null || acesso.contains(p.getUnidadeCodigo()))
                .forEach(p -> {
                    UnidadeParticipanteDto uDto = UnidadeParticipanteDto.fromSnapshot(p);
                    Subprocesso sp = mapSp.get(p.getUnidadeCodigo());
                    if (sp != null) {
                        uDto.setSituacaoSubprocesso(sp.getSituacao());
                        uDto.setDataLimite(sp.getDataLimiteEtapa1());
                        uDto.setCodSubprocesso(sp.getCodigo());
                        if (sp.getMapa() != null) uDto.setMapaCodigo(sp.getMapa().getCodigo());
                        uDto.setLocalizacaoAtualCodigo(obterLocalizacao(sp).getCodigo());
                    }
                    mapDto.put(p.getUnidadeCodigo(), uDto);
                });

        mapDto.values().forEach(u -> {
            UnidadeParticipanteDto pai = mapDto.get(u.getCodUnidadeSuperior());
            if (pai != null) pai.getFilhos().add(u);
            else dto.getUnidades().add(u);
        });
        dto.getUnidades().sort(Comparator.comparing(UnidadeParticipanteDto::getSigla));
        mapDto.values().forEach(u -> u.getFilhos().sort(Comparator.comparing(UnidadeParticipanteDto::getSigla)));
    }

    private Unidade obterLocalizacao(Subprocesso sp) {
        return subprocessoService.obterLocalizacaoAtual(sp);
    }

    private Set<Long> obterIdsUnidadesAcesso(Processo pr, Usuario us) {
        if (us.getPerfilAtivo() == Perfil.ADMIN) {
            return pr.getParticipantes().stream()
                    .map(UnidadeProcesso::getUnidadeCodigo)
                    .collect(Collectors.toSet());
        }
        Long root = us.getUnidadeAtivaCodigo();
        if (us.getPerfilAtivo() != Perfil.GESTOR) return Set.of(root);
        Set<Long> subarvore = new HashSet<>(buscarDescendentes(root));
        return pr.getParticipantes().stream().map(UnidadeProcesso::getUnidadeCodigo).filter(subarvore::contains).collect(Collectors.toSet());
    }

    private boolean isElegivelParaAcaoEmBloco(Subprocesso sp, Usuario us, Long codUnidadeUs) {
        SituacaoSubprocesso s = sp.getSituacao();
        boolean aceite = s == MAPEAMENTO_CADASTRO_DISPONIBILIZADO || s == REVISAO_CADASTRO_DISPONIBILIZADA ||
                (s.ordinal() >= MAPEAMENTO_MAPA_COM_SUGESTOES.ordinal() && s.ordinal() <= MAPEAMENTO_MAPA_VALIDADO.ordinal()) ||
                (s.ordinal() >= REVISAO_MAPA_COM_SUGESTOES.ordinal() && s.ordinal() <= REVISAO_MAPA_VALIDADO.ordinal());
        boolean disp = s == MAPEAMENTO_CADASTRO_HOMOLOGADO || s == MAPEAMENTO_MAPA_CRIADO || s == REVISAO_CADASTRO_HOMOLOGADA || s == REVISAO_MAPA_AJUSTADO;
        if (!aceite && !disp) return false;
        return us.getPerfilAtivo() == Perfil.ADMIN || Objects.equals(obterLocalizacao(sp).getCodigo(), codUnidadeUs);
    }

    private SubprocessoElegivelDto toElegivelDto(Subprocesso sp) {
        return SubprocessoElegivelDto.builder().codigo(sp.getCodigo()).unidadeCodigo(sp.getUnidade().getCodigo())
                .unidadeNome(sp.getUnidade().getNome()).unidadeSigla(sp.getUnidade().getSigla()).situacao(sp.getSituacao()).build();
    }

    private void notificarInicioProcesso(Processo p, List<Unidade> participantes) {
        log.info("Notificando início do processo {}", p.getCodigo());
        servicoAlertas.criarAlertasProcessoIniciado(p, participantes);
    }

    private void notificarFinalizacaoProcesso(Processo p) {
        log.info("Notificando finalização do processo {}", p.getCodigo());
        List<Long> unidadesCods = p.getParticipantes().stream().map(UnidadeProcesso::getUnidadeCodigo).toList();
        List<Unidade> participantes = unidadeService.porCodigos(unidadesCods);
        for (Unidade u : participantes) {
            servicoAlertas.criarAlertaAdmin(p, u, "Processo finalizado: " + p.getDescricao());
        }
    }

    private void processarAcoesBlocoAceiteHomologacao(AcaoEmBlocoRequest req, Usuario user, List<Subprocesso> list) {
        Map<Boolean, List<Long>> separacao = list.stream()
                .collect(Collectors.partitioningBy(
                        sp -> isSituacaoCadastro(sp.getSituacao()),
                        Collectors.mapping(Subprocesso::getCodigo, Collectors.toList())
                ));

        List<Long> cadastro = separacao.getOrDefault(true, List.of());
        List<Long> validacao = separacao.getOrDefault(false, List.of());

        if (req.acao() == ACEITAR) {
            if (!cadastro.isEmpty()) transicaoService.aceitarCadastroEmBloco(cadastro, user);
            if (!validacao.isEmpty()) transicaoService.aceitarValidacaoEmBloco(validacao, user);
        } else if (req.acao() == HOMOLOGAR) {
            if (!cadastro.isEmpty()) transicaoService.homologarCadastroEmBloco(cadastro, user);
            if (!validacao.isEmpty()) transicaoService.homologarValidacaoEmBloco(validacao, user);
        }
    }

    private boolean isSituacaoCadastro(SituacaoSubprocesso s) {
        return s == MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
               s == REVISAO_CADASTRO_DISPONIBILIZADA ||
               s == REVISAO_CADASTRO_HOMOLOGADA;
    }
    
    // Check permission helper
    public boolean checarAcesso(Authentication auth, Long cod) {
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Usuario usuario)) return false;
        if (usuario.getPerfilAtivo() == Perfil.ADMIN) return true;

        List<Long> unidadesAcesso = buscarCodigosAcesso(usuario);
        return processoRepo.buscarPorCodigoComParticipantes(cod)
                .map(p -> p.getParticipantes().stream()
                        .anyMatch(part -> unidadesAcesso.contains(part.getUnidadeCodigo())))
                .orElse(false);
    }

    private void validarSelecaoBloco(List<Long> codigos, List<Subprocesso> list) {
        if (codigos.size() != list.size()) {
            Set<Long> encontrados = list.stream().map(Subprocesso::getUnidade).map(Unidade::getCodigo).collect(Collectors.toSet());
            List<Long> faltando = codigos.stream().filter(codigo -> !encontrados.contains(codigo)).toList();
            throw new ErroValidacao(MsgValidacao.UNIDADES_SEM_SUBPROCESSOS.formatted(faltando));
        }
    }
}

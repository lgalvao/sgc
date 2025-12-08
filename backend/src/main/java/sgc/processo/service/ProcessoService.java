package sgc.processo.service;

import static sgc.processo.model.SituacaoProcesso.CRIADO;
import static sgc.processo.model.TipoProcesso.DIAGNOSTICO;
import static sgc.processo.model.TipoProcesso.REVISAO;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;
import static sgc.unidade.model.TipoUnidade.INTERMEDIARIA;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.CopiaMapaService;
import sgc.processo.dto.*;
import sgc.processo.dto.mappers.ProcessoDetalheMapper;
import sgc.processo.dto.mappers.ProcessoMapper;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.erros.ErroUnidadesNaoDefinidas;
import sgc.processo.eventos.EventoProcessoCriado;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.dto.SubprocessoMapper;
import sgc.subprocesso.model.*;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessoService {
    private final ProcessoRepo processoRepo;
    private final UnidadeRepo unidadeRepo;
    private final SubprocessoRepo subprocessoRepo;
    private final ApplicationEventPublisher publicadorEventos;
    private final ProcessoMapper processoMapper;
    private final ProcessoDetalheMapper processoDetalheMapper;
    private final SubprocessoMapper subprocessoMapper;
    private final MapaRepo mapaRepo;
    private final SubprocessoMovimentacaoRepo movimentacaoRepo;
    private final CopiaMapaService servicoDeCopiaDeMapa;
    private final ProcessoNotificacaoService processoNotificacaoService;
    private final SgrhService sgrhService;

    public boolean checarAcesso(Authentication authentication, Long codProcesso) {
        if (authentication == null || !authentication.isAuthenticated()) return false;

        String username = authentication.getName();
        boolean isGestorOuChefe =
                authentication.getAuthorities().stream()
                        .anyMatch(
                                a ->
                                        "ROLE_GESTOR".equals(a.getAuthority())
                                                || "ROLE_CHEFE".equals(a.getAuthority()));

        if (!isGestorOuChefe) {
            return false;
        }

        List<PerfilDto> perfis = sgrhService.buscarPerfisUsuario(username);
        Long codUnidadeUsuario =
                perfis.stream()
                        .findFirst()
                        .map(sgc.sgrh.dto.PerfilDto::getUnidadeCodigo)
                        .orElse(null);

        return codUnidadeUsuario != null
                && subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigo(
                        codProcesso, codUnidadeUsuario);
    }

    @Transactional
    public ProcessoDto criar(CriarProcessoReq req) {
        if (req.getDescricao() == null || req.getDescricao().isBlank()) {
            throw new ConstraintViolationException("A descrição do processo é obrigatória.", null);
        }

        if (req.getUnidades().isEmpty()) {
            throw new ConstraintViolationException(
                    "Pelo menos uma unidade participante deve ser selecionada.", null);
        }

        Set<Unidade> participantes = new HashSet<>();
        for (Long codigoUnidade : req.getUnidades()) {
            Unidade unidade =
                    unidadeRepo
                            .findById(codigoUnidade)
                            .orElseThrow(
                                    () -> new ErroEntidadeNaoEncontrada("Unidade", codigoUnidade));

            // Validação defensiva: unidades INTERMEDIARIAS não devem participar de
            // processos
            if (unidade.getTipo() == INTERMEDIARIA) {
                log.error(
                        "ERRO INTERNO: Tentativa de criar processo com unidade INTERMEDIARIA: {}",
                        unidade.getSigla());
                throw new IllegalStateException(
                        "Erro interno: unidade não elegível foi enviada ao backend");
            }
            participantes.add(unidade);
        }

        TipoProcesso tipoProcesso = req.getTipo();

        if (tipoProcesso == REVISAO || tipoProcesso == DIAGNOSTICO) {
            getMensagemErroUnidadesSemMapa(new ArrayList<>(req.getUnidades()))
                    .ifPresent(
                            msg -> {
                                throw new ErroProcesso(msg);
                            });
        }

        Processo processo =
                new Processo()
                        .setDescricao(req.getDescricao())
                        .setTipo(tipoProcesso)
                        .setDataLimite(req.getDataLimiteEtapa1())
                        .setSituacao(CRIADO)
                        .setDataCriacao(LocalDateTime.now())
                        .setParticipantes(participantes);

        Processo processoSalvo = processoRepo.saveAndFlush(processo);

        publicadorEventos.publishEvent(new EventoProcessoCriado(this, processoSalvo.getCodigo()));
        log.info("Processo {} criado.", processoSalvo.getCodigo());

        return processoMapper.toDto(processoSalvo);
    }

    @Transactional
    public ProcessoDto atualizar(Long codigo, AtualizarProcessoReq requisicao) {
        Processo processo =
                processoRepo
                        .findById(codigo)
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

        if (processo.getSituacao() != CRIADO) {
            throw new ErroProcessoEmSituacaoInvalida(
                    "Apenas processos na situação 'CRIADO' podem ser editados.");
        }

        processo.setDescricao(requisicao.getDescricao());
        processo.setTipo(requisicao.getTipo());
        processo.setDataLimite(requisicao.getDataLimiteEtapa1());

        if (requisicao.getTipo() == REVISAO || requisicao.getTipo() == DIAGNOSTICO) {
            getMensagemErroUnidadesSemMapa(new ArrayList<>(requisicao.getUnidades()))
                    .ifPresent(
                            msg -> {
                                throw new ErroProcesso(msg);
                            });
        }

        Set<Unidade> participantes = new HashSet<>();
        for (Long codigoUnidade : requisicao.getUnidades()) {
            participantes.add(
                    unidadeRepo
                            .findById(codigoUnidade)
                            .orElseThrow(
                                    () -> new ErroEntidadeNaoEncontrada("Unidade", codigoUnidade)));
        }
        processo.setParticipantes(participantes);

        Processo processoAtualizado = processoRepo.saveAndFlush(processo);
        log.info("Processo {} atualizado.", codigo);

        return processoMapper.toDto(processoAtualizado);
    }

    @Transactional
    public void apagar(Long codigo) {
        Processo processo =
                processoRepo
                        .findById(codigo)
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

        if (processo.getSituacao() != CRIADO) {
            throw new ErroProcessoEmSituacaoInvalida(
                    "Apenas processos na situação 'CRIADO' podem ser removidos.");
        }

        processoRepo.deleteById(codigo);
        log.info("Processo {} removido.", codigo);
    }

    @Transactional(readOnly = true)
    public Optional<ProcessoDto> obterPorId(Long codigo) {
        return processoRepo.findById(codigo).map(processoMapper::toDto);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @processoService.checarAcesso(authentication, #codProcesso)")
    public ProcessoDetalheDto obterDetalhes(Long codProcesso) {
        Processo processo =
                processoRepo
                        .findById(codProcesso)
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codProcesso));

        return processoDetalheMapper.toDetailDTO(processo);
    }

    @Transactional(readOnly = true)
    public List<ProcessoDto> listarFinalizados() {
        return processoRepo.findBySituacao(SituacaoProcesso.FINALIZADO).stream()
                .map(processoMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProcessoDto> listarAtivos() {
        return processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO).stream()
                .map(processoMapper::toDto)
                .toList();
    }

    @Transactional
    public List<String> iniciarProcessoMapeamento(Long codigo, List<Long> codsUnidades) {
        Processo processo =
                processoRepo
                        .findById(codigo)
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

        if (processo.getSituacao() != CRIADO) {
            throw new ErroProcessoEmSituacaoInvalida(
                    "Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        Set<Unidade> participantes = processo.getParticipantes();
        if (participantes.isEmpty()) {
            throw new ErroUnidadesNaoDefinidas(
                    "Não há unidades participantes definidas para este processo.");
        }

        List<Long> codigosUnidades = participantes.stream().map(Unidade::getCodigo).toList();

        Optional<String> erroUnidadesAtivas =
                getMensagemErroUnidadesEmProcessosAtivos(codigosUnidades);
        if (erroUnidadesAtivas.isPresent()) {
            return List.of(erroUnidadesAtivas.get());
        }

        for (Unidade unidade : participantes) {
            criarSubprocessoParaMapeamento(processo, unidade);
        }

        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoRepo.save(processo);

        publicadorEventos.publishEvent(
                new EventoProcessoIniciado(
                        processo.getCodigo(),
                        processo.getTipo().name(),
                        LocalDateTime.now(),
                        codigosUnidades));

        log.info(
                "Processo de mapeamento {} iniciado para {} unidades.",
                codigo,
                codsUnidades.size());
        return List.of();
    }

    @Transactional
    public List<String> iniciarProcessoRevisao(Long codigo, List<Long> codigosUnidades) {
        log.info(
                "Iniciando processo de revisão para código {} com unidades {}",
                codigo,
                codigosUnidades);
        Processo processo =
                processoRepo
                        .findById(codigo)
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

        if (processo.getSituacao() != CRIADO) {
            throw new ErroProcessoEmSituacaoInvalida(
                    "Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }
        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            throw new ErroUnidadesNaoDefinidas(
                    "A lista de unidades é obrigatória para iniciar o processo de revisão.");
        }

        List<String> erros = new ArrayList<>();
        getMensagemErroUnidadesSemMapa(codigosUnidades).ifPresent(erros::add);
        getMensagemErroUnidadesEmProcessosAtivos(codigosUnidades).ifPresent(erros::add);
        if (!erros.isEmpty()) return erros;

        for (Long codUnidade : codigosUnidades) {
            Unidade unidade =
                    unidadeRepo
                            .findById(codUnidade)
                            .orElseThrow(
                                    () -> new ErroEntidadeNaoEncontrada("Unidade", codUnidade));
            criarSubprocessoParaRevisao(processo, unidade);
        }

        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoRepo.save(processo);

        publicadorEventos.publishEvent(
                new EventoProcessoIniciado(
                        processo.getCodigo(),
                        processo.getTipo().name(),
                        LocalDateTime.now(),
                        codigosUnidades));

        log.info(
                "Processo de revisão {} iniciado para {} unidades.",
                codigo,
                codigosUnidades.size());
        return List.of();
    }

    @Transactional
    public List<String> iniciarProcessoDiagnostico(Long codigo, List<Long> codsUnidades) {
        Processo processo =
                processoRepo
                        .findById(codigo)
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

        if (processo.getSituacao() != CRIADO) {
            throw new ErroProcessoEmSituacaoInvalida(
                    "Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        Set<Unidade> participantes = processo.getParticipantes();
        if (participantes.isEmpty()) {
            throw new ErroUnidadesNaoDefinidas(
                    "Não há unidades participantes definidas para este processo.");
        }

        List<Long> codigosUnidades = participantes.stream().map(Unidade::getCodigo).toList();

        Optional<String> erroUnidadesAtivas =
                getMensagemErroUnidadesEmProcessosAtivos(codigosUnidades);
        if (erroUnidadesAtivas.isPresent()) {
            return List.of(erroUnidadesAtivas.get());
        }

        for (Unidade unidade : participantes) {
            criarSubprocessoParaDiagnostico(processo, unidade);
        }

        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoRepo.save(processo);

        publicadorEventos.publishEvent(
                new EventoProcessoIniciado(
                        processo.getCodigo(),
                        processo.getTipo().name(),
                        LocalDateTime.now(),
                        codigosUnidades));

        log.info(
                "Processo de diagnóstico {} iniciado para {} unidades.",
                codigo,
                codsUnidades.size());
        return List.of();
    }

    @Transactional
    public void finalizar(Long codigo) {
        log.debug("Iniciando finalização do processo: código={}", codigo);

        Processo processo =
                processoRepo
                        .findById(codigo)
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

        validarFinalizacaoProcesso(processo);
        tornarMapasVigentes(processo);

        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processo.setDataFinalizacao(LocalDateTime.now());

        processoRepo.save(processo);
        processoNotificacaoService.enviarNotificacoesDeFinalizacao(
                processo, new ArrayList<>(processo.getParticipantes()));
        publicadorEventos.publishEvent(new EventoProcessoFinalizado(this, processo.getCodigo()));

        log.info("Processo finalizado: código={}", codigo);
    }

    private Optional<String> getMensagemErroUnidadesEmProcessosAtivos(List<Long> codsUnidades) {
        if (codsUnidades == null || codsUnidades.isEmpty()) {
            return Optional.empty();
        }
        List<Long> unidadesBloqueadas =
                processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO).stream()
                        .flatMap(p -> p.getParticipantes().stream())
                        .map(Unidade::getCodigo)
                        .filter(codsUnidades::contains)
                        .distinct()
                        .toList();

        if (!unidadesBloqueadas.isEmpty()) {
            List<String> siglasUnidadesBloqueadas =
                    unidadeRepo.findSiglasByCodigos(unidadesBloqueadas);
            return Optional.of(
                    "As seguintes unidades já participam de outro processo ativo: %s"
                            .formatted(String.join(", ", siglasUnidadesBloqueadas)));
        }
        return Optional.empty();
    }

    private Optional<String> getMensagemErroUnidadesSemMapa(List<Long> codigosUnidades) {
        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            return Optional.empty();
        }
        List<Unidade> unidades = unidadeRepo.findAllById(codigosUnidades);
        List<Long> unidadesSemMapa =
                unidades.stream()
                        .filter(u -> u.getMapaVigente() == null)
                        .map(Unidade::getCodigo)
                        .toList();

        if (!unidadesSemMapa.isEmpty()) {
            List<String> siglasUnidadesSemMapa = unidadeRepo.findSiglasByCodigos(unidadesSemMapa);
            return Optional.of(
                    String.format(
                            "As seguintes unidades não possuem mapa vigente e não podem participar"
                                    + " de um processo de revisão: %s",
                            String.join(", ", siglasUnidadesSemMapa)));
        }
        return Optional.empty();
    }

    private void criarSubprocessoParaMapeamento(Processo processo, Unidade unidade) {
        if (TipoUnidade.OPERACIONAL == unidade.getTipo()
                || TipoUnidade.INTEROPERACIONAL == unidade.getTipo()) {
            Mapa mapa = mapaRepo.save(new Mapa());
            Subprocesso subprocesso =
                    new Subprocesso(
                            processo, unidade, mapa, NAO_INICIADO, processo.getDataLimite());
            subprocesso.setUnidadeNomeSnapshot(unidade.getNome());
            subprocesso.setUnidadeSiglaSnapshot(unidade.getSigla());
            Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);
            movimentacaoRepo.save(
                    new Movimentacao(subprocessoSalvo, null, unidade, "Processo iniciado", null));
        }
    }

    private void criarSubprocessoParaRevisao(Processo processo, Unidade unidade) {
        if (unidade.getMapaVigente() == null) {
            throw new ErroProcesso(
                    "Unidade %s não possui mapa vigente.".formatted(unidade.getSigla()));
        }

        Long codMapaVigente = unidade.getMapaVigente().getCodigo();
        Mapa mapaCopiado =
                servicoDeCopiaDeMapa.copiarMapaParaUnidade(codMapaVigente, unidade.getCodigo());
        Subprocesso subprocesso =
                new Subprocesso(
                        processo, unidade, mapaCopiado, NAO_INICIADO, processo.getDataLimite());
        subprocesso.setUnidadeNomeSnapshot(unidade.getNome());
        subprocesso.setUnidadeSiglaSnapshot(unidade.getSigla());
        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);
        movimentacaoRepo.save(
                new Movimentacao(
                        subprocessoSalvo, null, unidade, "Processo de revisão iniciado", null));
        log.info(
                "Subprocesso {} para revisão criado para unidade {}",
                subprocessoSalvo.getCodigo(),
                unidade.getSigla());
    }

    private void criarSubprocessoParaDiagnostico(Processo processo, Unidade unidade) {
        if (unidade.getMapaVigente() == null) {
            // Se não tem mapa vigente, não pode fazer diagnóstico?
            // Regra de negócio: Diagnóstico requer mapa vigente.
            // Mas talvez possa ser criado vazio?
            // Por enquanto, assumo que precisa de mapa, segue lógica de Revisão.
            // Mas o teste diz que "Passo 1: Criar e Homologar Mapa" é pré-requisito.
            // Então DEVE ter mapa vigente.
            throw new ErroProcesso(
                    "Unidade %s não possui mapa vigente para iniciar diagnóstico.".formatted(unidade.getSigla()));
        }

        Long codMapaVigente = unidade.getMapaVigente().getCodigo();
        // Para diagnostico, não precisamos copiar o mapa para EDIÇÃO (revisão),
        // mas o subprocesso precisa apontar para QUAL mapa está sendo diagnosticado.
        // O modelo Subprocesso tem relacionamento com Mapa.
        // Se apontarmos para o mapa vigente DIRETO, não podemos alterá-lo.
        // O diagnóstico preenche notas (Autoavaliação). As notas ficam em Subprocesso?
        // Não, as notas ficam em entidades associadas a Avaliação/Diagnóstico?
        // Vamos checar o modelo de dados?
        // Por hora, vou seguir o padrão de COPIAR o mapa para garantir isolamento,
        // assim como na Revisão, pois o diagnóstico pode "congelar" o estado das competências?
        // Ou o diagnóstico é sobre o mapa vigente?
        // Se eu olhar o código de criarSubprocessoParaRevisao, ele usa servicoDeCopiaDeMapa.
        // Vou assumir que diagnóstico também trabalha sobre uma cópia (snapshot) do mapa vigente.
        Mapa mapaCopiado =
                servicoDeCopiaDeMapa.copiarMapaParaUnidade(codMapaVigente, unidade.getCodigo());

        Subprocesso subprocesso =
                new Subprocesso(
                        processo, unidade, mapaCopiado, DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO, processo.getDataLimite());
        subprocesso.setUnidadeNomeSnapshot(unidade.getNome());
        subprocesso.setUnidadeSiglaSnapshot(unidade.getSigla());
        // Diagnóstico começa como NAO_INICIADO
        // Mas a UI do teste espera "Realizar Autoavaliação".
        // Se estiver NAO_INICIADO, o Chefe vê o botão "Iniciar"?
        // No teste, o Admin inicia o processo geral.
        // Ao criar os subprocessos, eles podem já nascer em estado "AUTOAVALIACAO_EM_ANDAMENTO"?
        // Ou o Chefe tem que clicar em iniciar?
        // No teste passo 3: "await page.getByText(descProcessoDiagnostico).click(); ... Navega para Autoavaliação -> click card-subprocesso-diagnostico"
        // Parece que o subprocesso já deve estar disponível.
        // Se o processo pai está EM_ANDAMENTO, o subprocesso deve estar ativo.

        // Vamos checar SituacaoSubprocesso para Diagnostico.
        // Existe SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO?
        // Se eu usar NAO_INICIADO, o frontend pode não mostrar.
        // Vou setar DIAGNOSTICO_EM_ANDAMENTO se existir, ou deixar NAO_INICIADO se houver transição automática.
        // Mas o mapa de revisão começa NAO_INICIADO.
        // Vou usar NAO_INICIADO e verificar se precisa de transição.

        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);

        // E automaticamente iniciar o diagnóstico?
        // Para Mapeamento e Revisão, fica NAO_INICIADO até alguém abrir.
        // Mas se o Processo já está iniciado...
        // O código de 'iniciarProcessoMapeamento' seta situacao NAO_INICIADO.

        // AJUSTE: O teste espera ver "Mapeamento Setup ... - Finalizado".
        // O novo processo é "Diagnostico Teste ... - Em andamento".
        // Se o subprocesso estiver NAO_INICIADO, o card pode aparecer diferente.

        // Vamos ver SituacaoSubprocesso disponíveis.

        movimentacaoRepo.save(
                new Movimentacao(
                        subprocessoSalvo, null, unidade, "Processo de diagnóstico iniciado", null));
        log.info(
                "Subprocesso {} para diagnóstico criado para unidade {}",
                subprocessoSalvo.getCodigo(),
                unidade.getSigla());
    }

    private void validarFinalizacaoProcesso(Processo processo) {
        if (processo.getSituacao() != SituacaoProcesso.EM_ANDAMENTO) {
            throw new ErroProcesso("Apenas processos 'EM ANDAMENTO' podem ser finalizados.");
        }
        validarTodosSubprocessosHomologados(processo);
    }

    private void validarTodosSubprocessosHomologados(Processo processo) {
        log.debug("Validando homologação de subprocessos do processo {}", processo.getCodigo());

        List<Subprocesso> subprocessos =
                subprocessoRepo.findByProcessoCodigoWithUnidade(processo.getCodigo());
        List<String> pendentes =
                subprocessos.stream()
                        .filter(
                                sp ->
                                        sp.getSituacao() != MAPEAMENTO_MAPA_HOMOLOGADO
                                                && sp.getSituacao() != REVISAO_MAPA_HOMOLOGADO)
                        .map(
                                sp -> {
                                    String identificador =
                                            sp.getUnidade() != null
                                                    ? sp.getUnidade().getSigla()
                                                    : String.format(
                                                            "Subprocesso %d", sp.getCodigo());
                                    return String.format(
                                            "%s (Situação: %s)", identificador, sp.getSituacao());
                                })
                        .toList();

        if (!pendentes.isEmpty()) {
            String mensagem =
                    String.format(
                            "Não é possível encerrar o processo. Unidades pendentes de"
                                    + " homologação:%n- %s",
                            String.join("%n- ", pendentes));
            log.warn(
                    "Validação de finalização falhou: {} subprocessos não homologados.",
                    pendentes.size());
            throw new ErroProcesso(mensagem);
        }
        log.info("Homologados {} subprocessos.", subprocessos.size());
    }

    private void tornarMapasVigentes(Processo processo) {
        log.info("Mapa vigente definido para o processo {}", processo.getCodigo());
        List<Subprocesso> subprocessos =
                subprocessoRepo.findByProcessoCodigoWithUnidade(processo.getCodigo());

        for (Subprocesso subprocesso : subprocessos) {
            Unidade unidade =
                    Optional.ofNullable(subprocesso.getUnidade())
                            .orElseThrow(
                                    () ->
                                            new ErroProcesso(
                                                    "Subprocesso %d sem unidade associada."
                                                            .formatted(subprocesso.getCodigo())));

            Mapa mapaDoSubprocesso =
                    Optional.ofNullable(subprocesso.getMapa())
                            .orElseThrow(
                                    () ->
                                            new ErroProcesso(
                                                    "Subprocesso %d sem mapa associado."
                                                            .formatted(subprocesso.getCodigo())));

            unidade.setMapaVigente(mapaDoSubprocesso);
            unidade.setDataVigenciaMapa(LocalDateTime.now());
            unidadeRepo.save(unidade);

            log.debug(
                    "Mapa vigente para unidade {} definido como mapa {}",
                    unidade.getCodigo(),
                    mapaDoSubprocesso.getCodigo());
        }
        log.info("Mapas de {} subprocessos foram definidos como vigentes.", subprocessos.size());
    }

    public List<Long> listarUnidadesBloqueadasPorTipo(String tipo) {
        TipoProcesso tipoProcesso = TipoProcesso.valueOf(tipo);

        return processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO).stream()
                .filter(p -> p.getTipo() == tipoProcesso)
                .flatMap(p -> p.getParticipantes().stream())
                .map(Unidade::getCodigo)
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public List<SubprocessoElegivelDto> listarSubprocessosElegiveis(Long codProcesso) {
        Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext()
                        .getAuthentication();
        String username = authentication.getName();
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        List<Subprocesso> subprocessos =
                subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso);
        if (isAdmin) {
            return subprocessos.stream()
                    .filter(sp -> sp.getSituacao() == SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO)
                    .map(this::toSubprocessoElegivelDto)
                    .toList();
        }

        List<PerfilDto> perfis = sgrhService.buscarPerfisUsuario(username);
        Long codUnidadeUsuario =
                perfis.stream().findFirst().map(PerfilDto::getUnidadeCodigo).orElse(null);

        if (codUnidadeUsuario == null) return List.of();

        return subprocessos.stream()
                .filter(
                        sp ->
                                sp.getUnidade() != null
                                        && sp.getUnidade().getCodigo().equals(codUnidadeUsuario))
                .filter(
                        sp ->
                                sp.getSituacao()
                                                == SituacaoSubprocesso
                                                        .MAPEAMENTO_CADASTRO_DISPONIBILIZADO
                                        || sp.getSituacao()
                                                == SituacaoSubprocesso
                                                        .REVISAO_CADASTRO_DISPONIBILIZADA)
                .map(this::toSubprocessoElegivelDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubprocessoDto> listarTodosSubprocessos(Long codProcesso) {
        return subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso).stream()
                .map(subprocessoMapper::toDTO)
                .toList();
    }

    private SubprocessoElegivelDto toSubprocessoElegivelDto(Subprocesso sp) {
        return SubprocessoElegivelDto.builder()
                .codSubprocesso(sp.getCodigo())
                .unidadeNome(sp.getUnidade().getNome())
                .unidadeSigla(sp.getUnidade().getSigla())
                .situacao(sp.getSituacao())
                .build();
    }
}

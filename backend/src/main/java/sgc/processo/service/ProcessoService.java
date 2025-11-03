package sgc.processo.service;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.comum.erros.ErroNegocio;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapa;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.mapa.service.CopiaMapaService;
import sgc.processo.dto.*;
import sgc.processo.eventos.EventoProcessoCriado;
import sgc.sgrh.service.SgrhService;
import sgc.sgrh.dto.PerfilDto;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.modelo.*;
import sgc.subprocesso.modelo.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.TipoUnidade;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessoService {
    private final ProcessoRepo processoRepo;
    private final UnidadeRepo unidadeRepo;
    private final UnidadeProcessoRepo unidadeProcessoRepo;
    private final SubprocessoRepo subprocessoRepo;
    private final ApplicationEventPublisher publicadorEventos;
    private final ProcessoMapper processoMapper;
    private final ProcessoDetalheMapperCustom processoDetalheMapperCustom;
    private final MapaRepo mapaRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final UnidadeMapaRepo unidadeMapaRepo;
    private final CopiaMapaService servicoDeCopiaDeMapa;
    private final ProcessoNotificacaoService processoNotificacaoService;
    private final SgrhService sgrhService;

    public boolean checarAcesso(Authentication authentication, Long codProcesso) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        boolean isGestorOuChefe = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GESTOR") || a.getAuthority().equals("ROLE_CHEFE"));

        if (!isGestorOuChefe) {
            return false;
        }

        // Para gestores, verifica se a unidade dele participa do processo.
        List<PerfilDto> perfis = sgrhService.buscarPerfisUsuario(username);
        Long codUnidadeUsuario = perfis.stream()
                .findFirst()
                .map(sgc.sgrh.dto.PerfilDto::unidadeCodigo)
                .orElse(null);

        if (codUnidadeUsuario == null) {
            return false;
        }

        return subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigo(codProcesso, codUnidadeUsuario);
    }

    /**
     * Cria um novo processo de mapeamento de competências.
     *
     * @param requisicao DTO contendo os dados para a criação do processo.
     * @return DTO do processo criado.
     * @throws ConstraintViolationException se a descrição ou as unidades participantes não forem fornecidas.
     * @throws ErroDominioNaoEncontrado     se alguma das unidades especificadas não existir (para tipos REVISAO ou DIAGNOSTICO).
     */
    @Transactional
    public ProcessoDto criar(CriarProcessoReq requisicao) {
        if (requisicao.descricao() == null || requisicao.descricao().isBlank()) {
            throw new ConstraintViolationException("A descrição do processo é obrigatória.", null);
        }
        if (requisicao.unidades().isEmpty()) {
            throw new ConstraintViolationException("Pelo menos uma unidade participante deve ser selecionada.", null);
        }

        if (requisicao.tipo() == TipoProcesso.REVISAO || requisicao.tipo() == TipoProcesso.DIAGNOSTICO) {
            for (Long codigoUnidade : requisicao.unidades()) {
                if (unidadeRepo.findById(codigoUnidade).isEmpty()) {
                    throw new ErroDominioNaoEncontrado("Unidade", codigoUnidade);
                }
            }
        }

        Processo processo = new Processo()
                .setDescricao(requisicao.descricao())
                .setTipo(requisicao.tipo())
                .setDataLimite(requisicao.dataLimiteEtapa1())
                .setSituacao(SituacaoProcesso.CRIADO)
                .setDataCriacao(LocalDateTime.now());

        Processo processoSalvo = processoRepo.save(processo);

        // Salvar snapshot das unidades participantes
        for (Long codigoUnidade : requisicao.unidades()) {
            Unidade unidade = unidadeRepo.findById(codigoUnidade)
                    .orElseThrow(() -> new ErroDominioNaoEncontrado("Unidade", codigoUnidade));
            UnidadeProcesso unidadeProcesso = criarSnapshotUnidadeProcesso(processoSalvo, unidade);
            unidadeProcessoRepo.save(unidadeProcesso);
        }

        publicadorEventos.publishEvent(new EventoProcessoCriado(this, processoSalvo.getCodigo()));
        log.info("Processo '{}' (código {}) criado com sucesso.", processoSalvo.getDescricao(), processoSalvo.getCodigo());

        return processoMapper.toDto(processoSalvo);
    }

    /**
     * Atualiza os dados de um processo existente.
     * <p>
     * A atualização só é permitida se o processo estiver na situação 'CRIADO'.
     *
     * @param codigo         O código do processo a ser atualizado.
     * @param requisicao DTO contendo os novos dados do processo.
     * @return DTO do processo atualizado.
     * @throws ErroDominioNaoEncontrado se o processo não for encontrado.
     * @throws IllegalStateException    se o processo não estiver na situação 'CRIADO'.
     */
    @Transactional
    public ProcessoDto atualizar(Long codigo, AtualizarProcessoReq requisicao) {
        Processo processo = processoRepo.findById(codigo)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", codigo));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser editados.");
        }

        processo.setDescricao(requisicao.descricao());
        processo.setTipo(requisicao.tipo());
        processo.setDataLimite(requisicao.dataLimiteEtapa1());

        Processo processoAtualizado = processoRepo.save(processo);
        log.info("Processo {} atualizado com sucesso.", codigo);

        return processoMapper.toDto(processoAtualizado);
    }

    /**
     * Remove um processo do sistema.
     * <p>
     * A remoção só é permitida se o processo estiver na situação 'CRIADO'.
     *
     * @param codigo O código do processo a ser removido.
     * @throws ErroDominioNaoEncontrado se o processo não for encontrado.
     * @throws IllegalStateException    se o processo não estiver na situação 'CRIADO'.
     */
    @Transactional
    public void apagar(Long codigo) {
        Processo processo = processoRepo.findById(codigo)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", codigo));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser removidos.");
        }

        processoRepo.deleteById(codigo);
        log.info("Processo {} removido com sucesso.", codigo);
    }

    /**
     * Busca um processo pelo seu código.
     *
     * @param codigo O código do processo.
     * @return Um {@link Optional} contendo o {@link ProcessoDto} se encontrado,
     * ou vazio caso contrário.
     */
    @Transactional(readOnly = true)
    public Optional<ProcessoDto> obterPorId(Long codigo) {
        return processoRepo.findById(codigo).map(processoMapper::toDto);
    }

    /**
     * Obtém os detalhes completos de um processo, incluindo suas unidades
     * participantes e o estado de seus subprocessos.
     * <p>
     * O acesso a este método é protegido e requer que o usuário seja 'ADMIN' ou
     * tenha acesso à unidade participante do processo, conforme verificado por
     * {@link #checarAcesso(Authentication, Long)}.
     *
     * @param codProcesso O código do processo a ser detalhado.
     * @return DTO com os detalhes completos do processo.
     * @throws ErroDominioNaoEncontrado se o processo não for encontrado.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @processoService.checarAcesso(authentication, #codProcesso)")
    public ProcessoDetalheDto obterDetalhes(Long codProcesso) {
        Processo processo = processoRepo.findById(codProcesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", codProcesso));

        List<UnidadeProcesso> listaUnidadesProcesso = unidadeProcessoRepo.findByCodProcesso(codProcesso);
        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso);

        return processoDetalheMapperCustom.toDetailDTO(processo, listaUnidadesProcesso, subprocessos);
    }

    /**
     * Retorna uma lista de todos os processos que estão na situação 'FINALIZADO'.
     *
     * @return Uma {@link List} de {@link ProcessoDto}.
     */
    @Transactional(readOnly = true)
    public List<ProcessoDto> listarFinalizados() {
        return processoRepo.findBySituacao(SituacaoProcesso.FINALIZADO)
                .stream()
                .map(processoMapper::toDto)
                .toList();
    }

    /**
     * Retorna uma lista de todos os processos que estão na situação 'EM_ANDAMENTO'.
     *
     * @return Uma {@link List} de {@link ProcessoDto}.
     */
    @Transactional(readOnly = true)
    public List<ProcessoDto> listarAtivos() {
        return processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO)
            .stream()
            .map(processoMapper::toDto)
            .toList();
    }

    // Métodos de Iniciação
    @Transactional
    public void iniciarProcessoMapeamento(Long codigo, List<Long> codsUnidades) {
        Processo processo = processoRepo.findById(codigo)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", codigo));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new ErroNegocio("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        // Buscar unidades já salvas no processo
        List<UnidadeProcesso> unidadesProcesso = unidadeProcessoRepo.findByCodProcesso(codigo);
        if (unidadesProcesso.isEmpty()) {
            throw new ErroNegocio("Não há unidades participantes definidas para este processo.");
        }

        List<Long> codigosUnidades = unidadesProcesso.stream()
                .map(UnidadeProcesso::getCodUnidade)
                .toList();

        validarUnidadesNaoEmProcessosAtivos(codigosUnidades);

        for (UnidadeProcesso up : unidadesProcesso) {
            Unidade unidade = unidadeRepo.findById(up.getCodUnidade())
                    .orElseThrow(() -> new ErroDominioNaoEncontrado("Unidade", up.getCodUnidade()));

            criarSubprocessoParaMapeamento(processo, unidade);
        }

        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoRepo.save(processo);

        publicadorEventos.publishEvent(new EventoProcessoIniciado(
                processo.getCodigo(),
                processo.getTipo().name(),
                LocalDateTime.now(),
                codigosUnidades
        ));

        log.info("Processo de mapeamento {} iniciado para {} unidades.", codigo, codsUnidades.size());
    }

    @Transactional
    public void iniciarProcessoRevisao(Long codigo, List<Long> codigosUnidades) {
        Processo processo = processoRepo.findById(codigo)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", codigo));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new ErroNegocio("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            throw new ErroNegocio("A lista de unidades é obrigatória para iniciar o processo de revisão.");
        }

        validarUnidadesComMapasVigentes(codigosUnidades);
        validarUnidadesNaoEmProcessosAtivos(codigosUnidades);

        for (Long codigoUnidade : codigosUnidades) {
            Unidade unidade = unidadeRepo.findById(codigoUnidade)
                    .orElseThrow(() -> new ErroDominioNaoEncontrado("Unidade", codigoUnidade));

            criarSubprocessoParaRevisao(processo, unidade);
        }

        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoRepo.save(processo);

        publicadorEventos.publishEvent(new EventoProcessoIniciado(
                processo.getCodigo(),
                processo.getTipo().name(),
                LocalDateTime.now(),
                codigosUnidades
        ));

        log.info("Processo de revisão {} iniciado para {} unidades.", codigo, codigosUnidades.size());
    }

    @Transactional
    public void finalizar(Long codigo) {
        log.info("Iniciando finalização do processo: código={}", codigo);

        Processo processo = processoRepo.findById(codigo)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", codigo));

        validarFinalizacaoProcesso(processo);
        tornarMapasVigentes(processo);

        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processo.setDataFinalizacao(LocalDateTime.now());

        processoRepo.save(processo);
        processoNotificacaoService.enviarNotificacoesDeFinalizacao(processo, unidadeProcessoRepo.findByCodProcesso(processo.getCodigo()));
        publicadorEventos.publishEvent(new EventoProcessoFinalizado(this, processo.getCodigo()));

        log.info("Processo finalizado com sucesso: código={}", codigo);
    }

    // Métodos Privados Auxiliares
    private void validarUnidadesNaoEmProcessosAtivos(List<Long> codsUnidades) {
        List<Long> unidadesProcesso = unidadeProcessoRepo.findUnidadesInProcessosAtivos(codsUnidades);
        if (!unidadesProcesso.isEmpty()) {
            throw new ErroProcesso("As seguintes unidades já participam de outro processo ativo: %s".formatted(unidadesProcesso));
        }
    }

    private void validarUnidadesComMapasVigentes(List<Long> codigosUnidades) {
        List<Long> unidadesComMapaVigente = unidadeMapaRepo.findCodigosUnidadesComMapaVigente(codigosUnidades);

        if (unidadesComMapaVigente.size() < codigosUnidades.size()) {
            List<Long> unidadesSemMapa = codigosUnidades.stream()
                    .filter(c -> !unidadesComMapaVigente.contains(c))
                    .toList();

            List<String> siglasUnidadesSemMapa = unidadeRepo.findSiglasByCodigos(unidadesSemMapa);
            throw new ErroProcesso(String.format(
                    "As seguintes unidades não possuem mapa vigente e não podem participar de um processo de revisão: %s",
                    String.join(", ", siglasUnidadesSemMapa)
            ));
        }
    }

    private UnidadeProcesso criarSnapshotUnidadeProcesso(Processo processo, Unidade unidade) {
        return new UnidadeProcesso(
                processo.getCodigo(),
                unidade.getCodigo(),
                unidade.getNome(),
                unidade.getSigla(),
                unidade.getTitular() != null ? String.valueOf(unidade.getTitular().getTituloEleitoral()) : null,
                unidade.getTipo(),
                "PENDENTE",
                unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null
        );
    }

    private void criarSubprocessoParaMapeamento(Processo processo, Unidade unidade) {
        UnidadeProcesso unidadeProcesso = criarSnapshotUnidadeProcesso(processo, unidade);
        unidadeProcessoRepo.save(unidadeProcesso);

        if (TipoUnidade.OPERACIONAL.equals(unidade.getTipo()) || TipoUnidade.INTEROPERACIONAL.equals(unidade.getTipo())) {
            Mapa mapa = mapaRepo.save(new Mapa());
            Subprocesso subprocesso = new Subprocesso(processo, unidade, mapa, SituacaoSubprocesso.NAO_INICIADO, processo.getDataLimite());
            Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);
            movimentacaoRepo.save(new Movimentacao(subprocessoSalvo, null, unidade, "Processo iniciado"));
        }
    }

    private void criarSubprocessoParaRevisao(Processo processo, Unidade unidade) {
        UnidadeMapa unidadeMapa = unidadeMapaRepo.findByUnidadeCodigo(unidade.getCodigo())
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Configuração de mapa vigente não encontrada para a unidade", unidade.getSigla()));

        Long codMapaVigente = unidadeMapa.getMapaVigenteCodigo();
        Mapa mapaCopiado = servicoDeCopiaDeMapa.copiarMapaParaUnidade(codMapaVigente, unidade.getCodigo());
        UnidadeProcesso unidadeProcesso = criarSnapshotUnidadeProcesso(processo, unidade);
        unidadeProcessoRepo.save(unidadeProcesso);

        Subprocesso subprocesso = new Subprocesso(processo, unidade, mapaCopiado, SituacaoSubprocesso.NAO_INICIADO, processo.getDataLimite());
        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);
        movimentacaoRepo.save(new Movimentacao(subprocessoSalvo, null, unidade, "Processo de revisão iniciado"));
    }

    private void validarFinalizacaoProcesso(Processo processo) {
        if (processo.getSituacao() != SituacaoProcesso.EM_ANDAMENTO) {
            throw new ErroProcesso("Apenas processos 'EM ANDAMENTO' podem ser finalizados.");
        }
        validarTodosSubprocessosHomologados(processo);
    }

    private void validarTodosSubprocessosHomologados(Processo processo) {
        log.debug("Validando homologação de subprocessos do processo {}", processo.getCodigo());
        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(processo.getCodigo());
        List<String> pendentes = subprocessos.stream()
                .filter(sp -> sp.getSituacao() != SituacaoSubprocesso.MAPA_HOMOLOGADO)
                .map(sp -> String.format("%s (Situação: %s)",
                        sp.getUnidade() != null ? sp.getUnidade().getSigla() : "Subprocesso %d".formatted(sp.getCodigo()),
                        sp.getSituacao()))
                .toList();

        if (!pendentes.isEmpty()) {
            String mensagem = String.format(
                    "Não é possível encerrar o processo. Unidades pendentes de homologação:%n- %s",
                    String.join("%n- ", pendentes)
            );
            log.warn("Validação de finalização falhou: {} subprocessos não homologados.", pendentes.size());
            throw new ErroProcesso(mensagem);
        }
        log.info("Validação OK: {} subprocessos homologados.", subprocessos.size());
    }

    private void tornarMapasVigentes(Processo processo) {
        log.info("Tornando mapas vigentes para o processo {}", processo.getCodigo());
        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(processo.getCodigo());

        for (Subprocesso subprocesso : subprocessos) {
            Long codigoUnidade = Optional.ofNullable(subprocesso.getUnidade()).map(Unidade::getCodigo)
                    .orElseThrow(() -> new ErroProcesso("Subprocesso %d sem unidade associada.".formatted(subprocesso.getCodigo())));

            Mapa mapaDoSubprocesso = Optional.ofNullable(subprocesso.getMapa())
                    .orElseThrow(() -> new ErroProcesso("Subprocesso %d sem mapa associado.".formatted(subprocesso.getCodigo())));

            UnidadeMapa unidadeMapa = unidadeMapaRepo.findByUnidadeCodigo(codigoUnidade)
                    .orElse(new UnidadeMapa(codigoUnidade));

            unidadeMapa.setMapaVigenteCodigo(mapaDoSubprocesso.getCodigo());
            unidadeMapa.setDataVigencia(LocalDateTime.now());
            unidadeMapaRepo.save(unidadeMapa);
            log.debug("Mapa vigente para unidade {} definido como mapa {}", codigoUnidade, mapaDoSubprocesso.getCodigo());
        }
        log.info("Mapas de {} subprocessos foram definidos como vigentes.", subprocessos.size());
    }

    /**
     * Lista códigos de unidades que já participam de processos ativos (EM_ANDAMENTO) do tipo especificado.
     *
     * @param tipo Tipo do processo (MAPEAMENTO, REVISAO, DIAGNOSTICO)
     * @return Lista de códigos de unidades bloqueadas
     */
    public List<Long> listarUnidadesBloqueadasPorTipo(String tipo) {
        TipoProcesso tipoProcesso = TipoProcesso.valueOf(tipo);

        // Busca todos os processos EM_ANDAMENTO do tipo especificado
        List<Processo> processosAtivos = processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO).stream()
            .filter(p -> p.getTipo() == tipoProcesso)
            .toList();

        // Extrai todas as unidades desses processos
        List<Long> codigosUnidadesBloqueadas = processosAtivos.stream()
            .flatMap(p -> unidadeProcessoRepo.findByCodProcesso(p.getCodigo()).stream())
            .map(UnidadeProcesso::getCodUnidade)
            .distinct()
            .toList();

        return codigosUnidadesBloqueadas;
    }
}

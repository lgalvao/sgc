package sgc.processo;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.processo.dto.*;
import sgc.processo.eventos.ProcessoCriadoEvento;
import sgc.processo.modelo.*;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.UnidadeRepo;
import sgc.mapa.CopiaMapaService;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapa;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.processo.eventos.ProcessoIniciadoEvento;
import sgc.processo.eventos.ProcessoFinalizadoEvento;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.unidade.modelo.TipoUnidade;
import sgc.unidade.modelo.Unidade;

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
    private final ApplicationEventPublisher publicadorDeEventos;
    private final ProcessoMapper processoMapper;
    private final ProcessoDetalheMapperCustom processoDetalheMapperCustom;
    private final MapaRepo mapaRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final UnidadeMapaRepo unidadeMapaRepo;
    private final CopiaMapaService servicoDeCopiaDeMapa;
    private final ProcessoNotificacaoService processoNotificacaoService;

    /**
     * Cria um novo processo de mapeamento de competências.
     *
     * @param requisicao DTO contendo os dados para a criação do processo.
     * @return DTO do processo criado.
     * @throws ConstraintViolationException se a descrição ou as unidades participantes não forem fornecidas.
     * @throws ErroDominioNaoEncontrado se alguma das unidades especificadas não existir (para tipos REVISAO ou DIAGNOSTICO).
     */
    @Transactional
    public ProcessoDto criar(CriarProcessoReq requisicao) {
        if (requisicao.descricao() == null || requisicao.descricao().isBlank()) {
            throw new ConstraintViolationException("A descrição do processo é obrigatória.", null);
        }
        if (requisicao.unidades().isEmpty()) {
            throw new ConstraintViolationException("Pelo menos uma unidade participante deve ser selecionada.", null);
        }

        if ("REVISAO".equalsIgnoreCase(requisicao.tipo()) || "DIAGNOSTICO".equalsIgnoreCase(requisicao.tipo())) {
            for (Long codigoUnidade : requisicao.unidades()) {
                if (unidadeRepo.findById(codigoUnidade).isEmpty()) {
                    throw new ErroDominioNaoEncontrado("Unidade", codigoUnidade);
                }
            }
        }

        Processo processo = new Processo();
        processo.setDescricao(requisicao.descricao());
        processo.setTipo(TipoProcesso.valueOf(requisicao.tipo()));
        processo.setDataLimite(requisicao.dataLimiteEtapa1());
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setDataCriacao(LocalDateTime.now());

        Processo processoSalvo = processoRepo.save(processo);

        publicadorDeEventos.publishEvent(new ProcessoCriadoEvento(this, processoSalvo.getCodigo()));
        log.info("Processo '{}' (código {}) criado com sucesso.", processoSalvo.getDescricao(), processoSalvo.getCodigo());

        return processoMapper.toDTO(processoSalvo);
    }

    /**
     * Atualiza os dados de um processo existente.
     * <p>
     * A atualização só é permitida se o processo estiver na situação 'CRIADO'.
     *
     * @param id         O ID do processo a ser atualizado.
     * @param requisicao DTO contendo os novos dados do processo.
     * @return DTO do processo atualizado.
     * @throws ErroDominioNaoEncontrado se o processo não for encontrado.
     * @throws IllegalStateException se o processo não estiver na situação 'CRIADO'.
     */
    @Transactional
    public ProcessoDto atualizar(Long id, AtualizarProcessoReq requisicao) {
        Processo processo = processoRepo.findById(id)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", id));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser editados.");
        }

        processo.setDescricao(requisicao.descricao());
        processo.setTipo(TipoProcesso.valueOf(requisicao.tipo()));
        processo.setDataLimite(requisicao.dataLimiteEtapa1());

        Processo processoAtualizado = processoRepo.save(processo);
        log.info("Processo {} atualizado com sucesso.", id);

        return processoMapper.toDTO(processoAtualizado);
    }

    /**
     * Remove um processo do sistema.
     * <p>
     * A remoção só é permitida se o processo estiver na situação 'CRIADO'.
     *
     * @param id O ID do processo a ser removido.
     * @throws ErroDominioNaoEncontrado se o processo não for encontrado.
     * @throws IllegalStateException se o processo não estiver na situação 'CRIADO'.
     */
    @Transactional
    public void apagar(Long id) {
        Processo processo = processoRepo.findById(id)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", id));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser removidos.");
        }

        processoRepo.deleteById(id);
        log.info("Processo {} removido com sucesso.", id);
    }

    /**
     * Busca um processo pelo seu ID.
     *
     * @param id O ID do processo.
     * @return Um {@link Optional} contendo o {@link ProcessoDto} se encontrado,
     *         ou vazio caso contrário.
     */
    @Transactional(readOnly = true)
    public Optional<ProcessoDto> obterPorId(Long id) {
        return processoRepo.findById(id).map(processoMapper::toDTO);
    }

    /**
     * Obtém os detalhes completos de um processo, incluindo suas unidades
     * participantes e o estado de seus subprocessos.
     * <p>
     * O acesso a este método é protegido e requer que o usuário seja 'ADMIN' ou
     * tenha acesso à unidade participante do processo, conforme verificado por
     * {@link ProcessoSeguranca#checarAcesso(Authentication, Long)}.
     *
     * @param idProcesso O ID do processo a ser detalhado.
     * @return DTO com os detalhes completos do processo.
     * @throws ErroDominioNaoEncontrado se o processo não for encontrado.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @processoSeguranca.checarAcesso(authentication, #idProcesso)")
    public ProcessoDetalheDto obterDetalhes(Long idProcesso) {
        Processo processo = processoRepo.findById(idProcesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", idProcesso));

        List<UnidadeProcesso> listaUnidadesProcesso = unidadeProcessoRepo.findByProcessoCodigo(idProcesso);
        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(idProcesso);

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
                .map(processoMapper::toDTO)
                .toList();
    }

    // Métodos de Iniciação
    @Transactional
    public void iniciarProcessoMapeamento(Long id, List<Long> codigosUnidades) {
        Processo processo = processoRepo.findById(id)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", id));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            throw new IllegalArgumentException("A lista de unidades é obrigatória para iniciar o processo de mapeamento.");
        }

        validarUnidadesNaoEmProcessosAtivos(codigosUnidades);

        for (Long codigoUnidade : codigosUnidades) {
            Unidade unidade = unidadeRepo.findById(codigoUnidade)
                    .orElseThrow(() -> new ErroDominioNaoEncontrado("Unidade", codigoUnidade));

            criarSubprocessoParaMapeamento(processo, unidade);
        }

        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoRepo.save(processo);

        publicadorDeEventos.publishEvent(new ProcessoIniciadoEvento(
                processo.getCodigo(),
                processo.getTipo().name(),
                LocalDateTime.now(),
                codigosUnidades
        ));

        log.info("Processo de mapeamento {} iniciado para {} unidades.", id, codigosUnidades.size());
    }

    @Transactional
    public void iniciarProcessoRevisao(Long id, List<Long> codigosUnidades) {
        Processo processo = processoRepo.findById(id)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", id));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            throw new IllegalArgumentException("A lista de unidades é obrigatória para iniciar o processo de revisão.");
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

        publicadorDeEventos.publishEvent(new ProcessoIniciadoEvento(
                processo.getCodigo(),
                processo.getTipo().name(),
                LocalDateTime.now(),
                codigosUnidades
        ));

        log.info("Processo de revisão {} iniciado para {} unidades.", id, codigosUnidades.size());
    }

    // Métodos de Finalização
    @Transactional
    public void finalizar(Long id) {
        log.info("Iniciando finalização do processo: código={}", id);

        Processo processo = processoRepo.findById(id)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", id));

        validarFinalizacaoProcesso(processo);
        tornarMapasVigentes(processo);

        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processo.setDataFinalizacao(LocalDateTime.now());

        processoRepo.save(processo);
        processoNotificacaoService.enviarNotificacoesDeFinalizacao(processo, unidadeProcessoRepo.findByProcessoCodigo(processo.getCodigo()));
        publicadorDeEventos.publishEvent(new ProcessoFinalizadoEvento(this, processo.getCodigo()));

        log.info("Processo finalizado com sucesso: código={}", id);
    }

    // Métodos Privados Auxiliares
    private void validarUnidadesNaoEmProcessosAtivos(List<Long> codigosUnidades) {
        List<Long> unidadesEmProcesso = unidadeProcessoRepo.findUnidadesInProcessosAtivos(codigosUnidades);
        if (!unidadesEmProcesso.isEmpty()) {
            throw new ErroProcesso("As seguintes unidades já participam de outro processo ativo: " + unidadesEmProcesso);
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
            .orElseThrow(() -> new IllegalStateException("Configuração de mapa vigente não encontrada para a unidade: " + unidade.getSigla()));
        Long idMapaVigente = unidadeMapa.getMapaVigenteCodigo();
        Mapa mapaCopiado = servicoDeCopiaDeMapa.copiarMapaParaUnidade(idMapaVigente, unidade.getCodigo());
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
                sp.getUnidade() != null ? sp.getUnidade().getSigla() : "Subprocesso " + sp.getCodigo(),
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
                .orElseThrow(() -> new ErroProcesso("Subprocesso " + subprocesso.getCodigo() + " sem unidade associada."));
            Mapa mapaDoSubprocesso = Optional.ofNullable(subprocesso.getMapa())
                .orElseThrow(() -> new ErroProcesso("Subprocesso " + subprocesso.getCodigo() + " sem mapa associado."));
            UnidadeMapa unidadeMapa = unidadeMapaRepo.findByUnidadeCodigo(codigoUnidade)
                .orElse(new UnidadeMapa(codigoUnidade));
            unidadeMapa.setMapaVigenteCodigo(mapaDoSubprocesso.getCodigo());
            unidadeMapa.setDataVigencia(LocalDateTime.now());
            unidadeMapaRepo.save(unidadeMapa);
            log.debug("Mapa vigente para unidade {} definido como mapa {}", codigoUnidade, mapaDoSubprocesso.getCodigo());
        }
        log.info("Mapas de {} subprocessos foram definidos como vigentes.", subprocessos.size());
    }
}

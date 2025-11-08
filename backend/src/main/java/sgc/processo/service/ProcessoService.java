package sgc.processo.service;

import jakarta.validation.ConstraintViolationException;
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
import sgc.mapa.model.UnidadeMapa;
import sgc.mapa.model.UnidadeMapaRepo;
import sgc.mapa.service.CopiaMapaService;
import sgc.processo.dto.AtualizarProcessoReq;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.dto.SubprocessoElegivelDto;
import sgc.processo.dto.mappers.ProcessoDetalheMapperCustom;
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
import sgc.subprocesso.model.*;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessoService {
    private final ProcessoRepo processoRepo;
    private final UnidadeRepo unidadeRepo;
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

    @SuppressWarnings("unused")
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

        List<PerfilDto> perfis = sgrhService.buscarPerfisUsuario(username);
        Long codUnidadeUsuario = perfis.stream()
                .findFirst()
                .map(sgc.sgrh.dto.PerfilDto::getUnidadeCodigo)
                .orElse(null);

        if (codUnidadeUsuario == null) {
            return false;
        }

        return subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigo(codProcesso, codUnidadeUsuario);
    }

    @Transactional
    public ProcessoDto criar(CriarProcessoReq requisicao) {
        if (requisicao.getDescricao() == null || requisicao.getDescricao().isBlank()) {
            throw new ConstraintViolationException("A descrição do processo é obrigatória.", null);
        }
        if (requisicao.getUnidades().isEmpty()) {
            throw new ConstraintViolationException("Pelo menos uma unidade participante deve ser selecionada.", null);
        }

        Set<Unidade> participantes = new HashSet<>();
        for (Long codigoUnidade : requisicao.getUnidades()) {
            participantes.add(unidadeRepo.findById(codigoUnidade)
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", codigoUnidade)));
        }

        Processo processo = new Processo();
        processo.setDescricao(requisicao.getDescricao());
        processo.setTipo(requisicao.getTipo());
        processo.setDataLimite(requisicao.getDataLimiteEtapa1());
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setDataCriacao(LocalDateTime.now());
        processo.setParticipantes(participantes);

        Processo processoSalvo = processoRepo.save(processo);

        publicadorEventos.publishEvent(new EventoProcessoCriado(this, processoSalvo.getCodigo()));
        log.info("Processo '{}' (código {}) criado.", processoSalvo.getDescricao(), processoSalvo.getCodigo());

        return processoMapper.toDto(processoSalvo);
    }

    @Transactional
    public ProcessoDto atualizar(Long codigo, AtualizarProcessoReq requisicao) {
        Processo processo = processoRepo.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser editados.");
        }

        processo.setDescricao(requisicao.getDescricao());
        processo.setTipo(requisicao.getTipo());
        processo.setDataLimite(requisicao.getDataLimiteEtapa1());

        Set<Unidade> participantes = new HashSet<>();
        for (Long codigoUnidade : requisicao.getUnidades()) {
            participantes.add(unidadeRepo.findById(codigoUnidade)
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", codigoUnidade)));
        }
        processo.setParticipantes(participantes);

        Processo processoAtualizado = processoRepo.save(processo);
        log.info("Processo {} atualizado.", codigo);

        return processoMapper.toDto(processoAtualizado);
    }

    @Transactional
    public void apagar(Long codigo) {
        Processo processo = processoRepo.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser removidos.");
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
        Processo processo = processoRepo.findById(codProcesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codProcesso));

        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso);

        return processoDetalheMapperCustom.toDetailDTO(processo);
    }

    @Transactional(readOnly = true)
    public List<ProcessoDto> listarFinalizados() {
        return processoRepo.findBySituacao(SituacaoProcesso.FINALIZADO)
                .stream()
                .map(processoMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProcessoDto> listarAtivos() {
        return processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO)
            .stream()
            .map(processoMapper::toDto)
            .toList();
    }

    @Transactional
    public void iniciarProcessoMapeamento(Long codigo, List<Long> codsUnidades) {
        Processo processo = processoRepo.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new ErroProcessoEmSituacaoInvalida("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        Set<Unidade> participantes = processo.getParticipantes();
        if (participantes.isEmpty()) {
            throw new ErroUnidadesNaoDefinidas("Não há unidades participantes definidas para este processo.");
        }

        List<Long> codigosUnidades = participantes.stream()
                .map(Unidade::getCodigo)
                .toList();

        validarUnidadesNaoEmProcessosAtivos(codigosUnidades);

        for (Unidade unidade : participantes) {
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
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new ErroProcessoEmSituacaoInvalida("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            throw new ErroUnidadesNaoDefinidas("A lista de unidades é obrigatória para iniciar o processo de revisão.");
        }

        validarUnidadesComMapasVigentes(codigosUnidades);
        validarUnidadesNaoEmProcessosAtivos(codigosUnidades);

        for (Long codigoUnidade : codigosUnidades) {
            Unidade unidade = unidadeRepo.findById(codigoUnidade)
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", codigoUnidade));

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
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

        validarFinalizacaoProcesso(processo);
        tornarMapasVigentes(processo);

        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processo.setDataFinalizacao(LocalDateTime.now());

        processoRepo.save(processo);
        processoNotificacaoService.enviarNotificacoesDeFinalizacao(processo, new ArrayList<>(processo.getParticipantes()));
        publicadorEventos.publishEvent(new EventoProcessoFinalizado(this, processo.getCodigo()));

        log.info("Processo finalizado: código={}", codigo);
    }

    private void validarUnidadesNaoEmProcessosAtivos(List<Long> codsUnidades) {
        List<Long> unidadesBloqueadas = processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO).stream()
            .flatMap(p -> p.getParticipantes().stream())
            .map(Unidade::getCodigo)
            .filter(codsUnidades::contains)
            .distinct()
            .toList();
        if (!unidadesBloqueadas.isEmpty()) {
            throw new ErroProcesso("As seguintes unidades já participam de outro processo ativo: %s".formatted(unidadesBloqueadas));
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

    private void criarSubprocessoParaMapeamento(Processo processo, Unidade unidade) {
        if (TipoUnidade.OPERACIONAL.equals(unidade.getTipo()) || TipoUnidade.INTEROPERACIONAL.equals(unidade.getTipo())) {
            Mapa mapa = mapaRepo.save(new Mapa());
            Subprocesso subprocesso = new Subprocesso(processo, unidade, mapa, SituacaoSubprocesso.NAO_INICIADO, processo.getDataLimite());
            Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);
            movimentacaoRepo.save(new Movimentacao(subprocessoSalvo, null, unidade, "Processo iniciado"));
        }
    }

    private void criarSubprocessoParaRevisao(Processo processo, Unidade unidade) {
        UnidadeMapa unidadeMapa = unidadeMapaRepo.findByUnidadeCodigo(unidade.getCodigo())
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Configuração de mapa vigente não encontrada para a unidade", unidade.getSigla()));

        Long codMapaVigente = unidadeMapa.getMapaVigente().getCodigo();
        Mapa mapaCopiado = servicoDeCopiaDeMapa.copiarMapaParaUnidade(codMapaVigente, unidade.getCodigo());

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
            Unidade unidade = Optional.ofNullable(subprocesso.getUnidade())
                    .orElseThrow(() -> new ErroProcesso("Subprocesso %d sem unidade associada.".formatted(subprocesso.getCodigo())));

            Mapa mapaDoSubprocesso = Optional.ofNullable(subprocesso.getMapa())
                    .orElseThrow(() -> new ErroProcesso("Subprocesso %d sem mapa associado.".formatted(subprocesso.getCodigo())));

            UnidadeMapa unidadeMapa = unidadeMapaRepo.findByUnidadeCodigo(unidade.getCodigo())
                    .orElse(new UnidadeMapa());
            unidadeMapa.setUnidade(unidade);
            unidadeMapa.setMapaVigente(mapaDoSubprocesso);
            unidadeMapa.setDataVigencia(LocalDateTime.now());
            unidadeMapaRepo.save(unidadeMapa);
            log.debug("Mapa vigente para unidade {} definido como mapa {}", unidade.getCodigo(), mapaDoSubprocesso.getCodigo());
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
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso);

        if (isAdmin) {
            return subprocessos.stream()
                .filter(sp -> sp.getSituacao() == SituacaoSubprocesso.MAPA_AJUSTADO)
                .map(this::toSubprocessoElegivelDto)
                .toList();
        }

        List<PerfilDto> perfis = sgrhService.buscarPerfisUsuario(username);
        Long codUnidadeUsuario = perfis.stream()
            .findFirst()
            .map(PerfilDto::getUnidadeCodigo)
            .orElse(null);

        if (codUnidadeUsuario == null) {
            return List.of();
        }

        return subprocessos.stream()
            .filter(sp -> sp.getUnidade() != null && sp.getUnidade().getCodigo().equals(codUnidadeUsuario))
            .filter(sp -> sp.getSituacao() == SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO)
            .map(this::toSubprocessoElegivelDto)
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

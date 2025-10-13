package sgc.processo;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.CopiaMapaService;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapa;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.notificacao.NotificacaoModeloEmailService;
import sgc.notificacao.NotificacaoService;
import sgc.processo.dto.*;
import sgc.processo.eventos.ProcessoCriadoEvento;
import sgc.processo.eventos.ProcessoFinalizadoEvento;
import sgc.processo.eventos.ProcessoIniciadoEvento;
import sgc.processo.modelo.*;
import sgc.sgrh.SgrhService;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.TipoUnidade;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessoService {
    private final ProcessoRepo processoRepo;
    private final UnidadeRepo unidadeRepo;
    private final UnidadeProcessoRepo unidadeProcessoRepo;
    private final SubprocessoRepo subprocessoRepo;
    private final MapaRepo mapaRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final UnidadeMapaRepo unidadeMapaRepo;
    private final CopiaMapaService servicoDeCopiaDeMapa;
    private final ApplicationEventPublisher publicadorDeEventos;
    private final NotificacaoService notificacaoService;
    private final NotificacaoModeloEmailService notificacaoModeloEmailService;
    private final SgrhService sgrhService;
    private final ProcessoMapper processoMapper;
    private final ProcessoDetalheMapperCustom processoDetalheMapperCustom;

    @Transactional
    public ProcessoDto criar(CriarProcessoReq requisicao) {
        if (requisicao.descricao() == null || requisicao.descricao().isBlank()) {
            throw new ConstraintViolationException("A descrição do processo é obrigatória.", null);
        }
        if (requisicao.unidades() == null || requisicao.unidades().isEmpty()) {
            throw new ConstraintViolationException("Pelo menos uma unidade participante deve ser selecionada.", null);
        }

        if ("REVISAO".equalsIgnoreCase(requisicao.tipo()) || "DIAGNOSTICO".equalsIgnoreCase(requisicao.tipo())) {
            for (Long codigoUnidade : requisicao.unidades()) {
                if (unidadeRepo.findById(codigoUnidade).isEmpty()) {
                    throw new ErroEntidadeNaoEncontrada("Unidade com código " + codigoUnidade + " não foi encontrada.");
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

    @Transactional
    public ProcessoDto atualizar(Long id, AtualizarProcessoReq requisicao) {
        Processo processo = processoRepo.findById(id)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo não encontrado: " + id));

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

    @Transactional
    public void apagar(Long id) {
        Processo processo = processoRepo.findById(id)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo não encontrado: " + id));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser removidos.");
        }

        processoRepo.deleteById(id);
        log.info("Processo {} removido com sucesso.", id);
    }

    @Transactional(readOnly = true)
    public Optional<ProcessoDto> obterPorId(Long id) {
        return processoRepo.findById(id).map(processoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @processoSeguranca.checarAcesso(authentication, #idProcesso)")
    public ProcessoDetalheDto obterDetalhes(Long idProcesso) {
        Processo processo = processoRepo.findById(idProcesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo não encontrado: " + idProcesso));

        List<UnidadeProcesso> listaUnidadesProcesso = unidadeProcessoRepo.findByProcessoCodigo(idProcesso);
        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(idProcesso);

        return processoDetalheMapperCustom.toDetailDTO(processo, listaUnidadesProcesso, subprocessos);
    }

    @Transactional
    public ProcessoDto iniciarProcessoMapeamento(Long id, List<Long> codigosUnidades) {
        Processo processo = processoRepo.findById(id)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo não encontrado: " + id));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            throw new IllegalArgumentException("A lista de unidades é obrigatória para iniciar o processo de mapeamento.");
        }

        validarUnidadesNaoEmProcessosAtivos(codigosUnidades);

        for (Long codigoUnidade : codigosUnidades) {
            Unidade unidade = unidadeRepo.findById(codigoUnidade)
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade não encontrada: " + codigoUnidade));

            criarSubprocessoParaMapeamento(processo, unidade);
        }

        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        Processo processoSalvo = processoRepo.save(processo);

        publicadorDeEventos.publishEvent(new ProcessoIniciadoEvento(
                processoSalvo.getCodigo(),
                processoSalvo.getTipo().name(),
                LocalDateTime.now(),
                codigosUnidades
        ));

        log.info("Processo de mapeamento {} iniciado para {} unidades.", id, codigosUnidades.size());
        return processoMapper.toDTO(processoSalvo);
    }

    @Transactional
    public ProcessoDto iniciarProcessoRevisao(Long id, List<Long> codigosUnidades) {
        Processo processo = processoRepo.findById(id)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo não encontrado: " + id));

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
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade não encontrada: " + codigoUnidade));

            criarSubprocessoParaRevisao(processo, unidade);
        }

        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        Processo processoSalvo = processoRepo.save(processo);

        publicadorDeEventos.publishEvent(new ProcessoIniciadoEvento(
                processoSalvo.getCodigo(),
                processoSalvo.getTipo().name(),
                LocalDateTime.now(),
                codigosUnidades
        ));

        log.info("Processo de revisão {} iniciado para {} unidades.", id, codigosUnidades.size());
        return processoMapper.toDTO(processoSalvo);
    }

    @Transactional
    public ProcessoDto finalizar(Long id) {
        log.info("Iniciando finalização do processo: código={}", id);

        Processo processo = processoRepo.findById(id)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo não encontrado: " + id));

        validarFinalizacaoProcesso(processo);
        tornarMapasVigentes(processo);

        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processo.setDataFinalizacao(LocalDateTime.now());
        processo = processoRepo.save(processo);

        enviarNotificacoesDeFinalizacao(processo);

        publicadorDeEventos.publishEvent(new ProcessoFinalizadoEvento(this, processo.getCodigo()));

        log.info("Processo finalizado com sucesso: código={}", id);
        return processoMapper.toDTO(processo);
    }

    private void validarUnidadesNaoEmProcessosAtivos(List<Long> codigosUnidades) {
        List<Long> unidadesEmProcesso = unidadeProcessoRepo.findUnidadesInProcessosAtivos(codigosUnidades);
        if (!unidadesEmProcesso.isEmpty()) {
            throw new ErroProcesso("As seguintes unidades já participam de outro processo ativo: " + unidadesEmProcesso);
        }
    }

    private void validarUnidadesComMapasVigentes(List<Long> codigosUnidades) {
        // Busca todos os mapas vigentes para as unidades em uma única consulta.
        List<Long> unidadesComMapaVigente = unidadeMapaRepo.findCodigosUnidadesComMapaVigente(codigosUnidades);

        if (unidadesComMapaVigente.size() < codigosUnidades.size()) {
            List<Long> unidadesSemMapa = codigosUnidades.stream()
                .filter(c -> !unidadesComMapaVigente.contains(c))
                .toList();

            // Busca as siglas das unidades sem mapa em uma única consulta.
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
            unidade.getCodigo(), // Adicionado o código da unidade
            unidade.getNome(),
            unidade.getSigla(),
            unidade.getTitular() != null ? unidade.getTitular().getTitulo() : null,
            unidade.getTipo(),
            "PENDENTE",
            unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null
        );
    }

    private void validarTodosSubprocessosHomologados(Processo processo) {
        log.debug("Validando homologação de subprocessos do processo {}", processo.getCodigo());
        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigo(processo.getCodigo());

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
        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigo(processo.getCodigo());

        for (Subprocesso subprocesso : subprocessos) {
            Long codigoUnidade = Optional.ofNullable(subprocesso.getUnidade()).map(Unidade::getCodigo)
                .orElseThrow(() -> new ErroProcesso("Subprocesso " + subprocesso.getCodigo() + " sem unidade associada."));

            Mapa mapaDoSubprocesso = Optional.ofNullable(subprocesso.getMapa())
                .orElseThrow(() -> new ErroProcesso("Subprocesso " + subprocesso.getCodigo() + " sem mapa associado."));

            UnidadeMapa unidadeMapa = unidadeMapaRepo.findByUnidadeCodigo(codigoUnidade)
                .orElse(new UnidadeMapa(codigoUnidade));

            unidadeMapa.setMapaVigenteCodigo(mapaDoSubprocesso.getCodigo());
            unidadeMapa.setDataVigencia(LocalDate.now());
            unidadeMapaRepo.save(unidadeMapa);

            log.debug("Mapa vigente para unidade {} definido como mapa {}", codigoUnidade, mapaDoSubprocesso.getCodigo());
        }
        log.info("Mapas de {} subprocessos foram definidos como vigentes.", subprocessos.size());
    }

    private void enviarNotificacoesDeFinalizacao(Processo processo) {
        log.info("Enviando notificações de finalização para o processo {}", processo.getCodigo());
        List<UnidadeProcesso> unidadesParticipantes = unidadeProcessoRepo.findByProcessoCodigo(processo.getCodigo());

        if (unidadesParticipantes.isEmpty()) {
            log.warn("Nenhuma unidade participante encontrada para notificar na finalização do processo {}", processo.getCodigo());
            return;
        }

        List<Long> todosCodigosUnidades = unidadesParticipantes.stream().map(UnidadeProcesso::getUnidadeCodigo).toList();
        Map<Long, ResponsavelDto> responsaveis = sgrhService.buscarResponsaveisUnidades(todosCodigosUnidades);
        Map<String, UsuarioDto> usuarios = sgrhService.buscarUsuariosPorTitulos(
            responsaveis.values().stream().map(ResponsavelDto::titularTitulo).distinct().toList()
        );

        for (UnidadeProcesso unidade : unidadesParticipantes) {
            try {
                ResponsavelDto responsavel = Optional.ofNullable(responsaveis.get(unidade.getUnidadeCodigo()))
                    .orElseThrow(() -> new IllegalStateException("Responsável não encontrado para a unidade " + unidade.getSigla()));

                UsuarioDto titular = Optional.ofNullable(usuarios.get(responsavel.titularTitulo()))
                    .orElseThrow(() -> new IllegalStateException("Usuário titular não encontrado: " + responsavel.titularTitulo()));

                String emailTitular = Optional.ofNullable(titular.email()).filter(e -> !e.isBlank())
                    .orElseThrow(() -> new IllegalStateException("E-mail não cadastrado para o titular " + titular.nome()));

                if (unidade.getTipo() == TipoUnidade.OPERACIONAL || unidade.getTipo() == TipoUnidade.INTEROPERACIONAL) {
                    enviarEmailUnidadeFinal(processo, unidade, emailTitular);
                } else if (unidade.getTipo() == TipoUnidade.INTERMEDIARIA) {
                    enviarEmailUnidadeIntermediaria(processo, unidade, emailTitular, unidadesParticipantes);
                }

            } catch (Exception ex) {
                log.error("Falha ao preparar notificação para unidade {} no processo {}: {}", unidade.getSigla(), processo.getCodigo(), ex.getMessage(), ex);
            }
        }
    }

    private void enviarEmailUnidadeFinal(Processo processo, UnidadeProcesso unidade, String email) {
        String assunto = String.format("SGC: Conclusão do processo %s", processo.getDescricao());
        String html = notificacaoModeloEmailService.criarEmailDeProcessoFinalizadoPorUnidade(
            unidade.getSigla(),
            processo.getDescricao()
        );
        notificacaoService.enviarEmailHtml(email, assunto, html);
        log.info("E-mail de finalização (unidade final) enviado para {} ({})", unidade.getSigla(), email);
    }

    private void enviarEmailUnidadeIntermediaria(Processo processo, UnidadeProcesso unidadeIntermediaria, String email, List<UnidadeProcesso> todasUnidades) {
        List<String> siglasSubordinadas = todasUnidades.stream()
            .filter(u -> unidadeIntermediaria.getUnidadeCodigo().equals(u.getUnidadeSuperiorCodigo()))
            .map(UnidadeProcesso::getSigla)
            .sorted()
            .toList();

        if (siglasSubordinadas.isEmpty()) {
            log.info("Nenhuma unidade subordinada encontrada para notificar a unidade intermediária {}", unidadeIntermediaria.getSigla());
            return;
        }

        String assunto = String.format("SGC: Conclusão do processo %s em unidades subordinadas", processo.getDescricao());
        String html = notificacaoModeloEmailService.criarEmailDeProcessoFinalizadoUnidadesSubordinadas(
            unidadeIntermediaria.getSigla(),
            processo.getDescricao(),
            siglasSubordinadas
        );

        notificacaoService.enviarEmailHtml(email, assunto, html);
        log.info("E-mail de finalização (unidade intermediária) enviado para {} ({})", unidadeIntermediaria.getSigla(), email);
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
        // Para revisão, um novo mapa é criado para a unidade, não copiado.
        Mapa mapaNovo = mapaRepo.save(new Mapa());

        UnidadeProcesso unidadeProcesso = criarSnapshotUnidadeProcesso(processo, unidade);
        unidadeProcessoRepo.save(unidadeProcesso);

        Subprocesso subprocesso = new Subprocesso(processo, unidade, mapaNovo, SituacaoSubprocesso.NAO_INICIADO, processo.getDataLimite());
        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);

        movimentacaoRepo.save(new Movimentacao(subprocessoSalvo, null, unidade, "Processo de revisão iniciado"));
    }

    private void validarFinalizacaoProcesso(Processo processo) {
        if (processo.getSituacao() != SituacaoProcesso.EM_ANDAMENTO) {
            throw new IllegalStateException("Apenas processos 'EM ANDAMENTO' podem ser finalizados.");
        }
        validarTodosSubprocessosHomologados(processo);
    }
}

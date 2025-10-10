package sgc.processo;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.enums.SituacaoProcesso;
import sgc.comum.enums.SituacaoSubprocesso;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.CopiaMapaService;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapa;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.notificacao.NotificacaoEmailService;
import sgc.notificacao.NotificacaoTemplateEmailService;
import sgc.processo.dto.*;
import sgc.processo.enums.TipoProcesso;
import sgc.processo.eventos.ProcessoCriadoEvento;
import sgc.processo.eventos.ProcessoFinalizadoEvento;
import sgc.processo.eventos.ProcessoIniciadoEvento;
import sgc.processo.modelo.*;
import sgc.sgrh.SgrhService;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import sgc.unidade.enums.TipoUnidade;

import java.time.LocalDate;
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
    private final MapaRepo mapaRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final UnidadeMapaRepo unidadeMapaRepo;
    private final CopiaMapaService servicoDeCopiaDeMapa;
    private final ApplicationEventPublisher publicadorDeEventos;
    private final NotificacaoEmailService servicoNotificacaoEmail;
    private final NotificacaoTemplateEmailService notificacaoTemplateEmailService;
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
    public ProcessoDetalheDto obterDetalhes(Long idProcesso, String perfil, Long idUnidadeUsuario) {
        if (perfil == null || perfil.isBlank()) {
            throw new ErroDominioAccessoNegado("Perfil inválido para acesso aos detalhes do processo.");
        }

        Processo processo = processoRepo.findById(idProcesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo não encontrado: " + idProcesso));

        if ("GESTOR".equalsIgnoreCase(perfil)) {
            boolean unidadeDoUsuarioParticipa = subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigo(idProcesso, idUnidadeUsuario);
            if (!unidadeDoUsuarioParticipa) {
                throw new ErroDominioAccessoNegado("Acesso negado. Sua unidade não participa deste processo.");
            }
        } else if (!"ADMIN".equalsIgnoreCase(perfil)) {
            throw new ErroDominioAccessoNegado("Acesso negado. Perfil sem permissão para ver detalhes do processo.");
        }

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

            UnidadeProcesso unidadeProcesso = criarSnapshotUnidadeProcesso(processo, unidade);
            unidadeProcessoRepo.save(unidadeProcesso);

            if (TipoUnidade.OPERACIONAL.equals(unidade.getTipo()) || TipoUnidade.INTEROPERACIONAL.equals(unidade.getTipo())) {
                Mapa mapa = mapaRepo.save(new Mapa());

                Subprocesso subprocesso = new Subprocesso(processo, unidade, mapa, SituacaoSubprocesso.NAO_INICIADO, processo.getDataLimite());
                Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);

                movimentacaoRepo.save(new Movimentacao(subprocessoSalvo, null, unidade, "Processo iniciado"));
            }
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

        validarUnidadesNaoEmProcessosAtivos(codigosUnidades);
        validarUnidadesComMapasVigentes(codigosUnidades);

        for (Long codigoUnidade : codigosUnidades) {
            Unidade unidade = unidadeRepo.findById(codigoUnidade)
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade não encontrada: " + codigoUnidade));

            Long idMapaOrigem = unidadeMapaRepo.findByUnidadeCodigo(codigoUnidade)
                    .map(UnidadeMapa::getMapaVigenteCodigo)
                    .orElseThrow(() -> new ErroProcesso("Mapa vigente não encontrado para a unidade: " + codigoUnidade));

            Mapa mapaNovo = servicoDeCopiaDeMapa.copiarMapaParaUnidade(idMapaOrigem, codigoUnidade);

            UnidadeProcesso unidadeProcesso = criarSnapshotUnidadeProcesso(processo, unidade);
            unidadeProcessoRepo.save(unidadeProcesso);

            Subprocesso subprocesso = new Subprocesso(processo, unidade, mapaNovo, SituacaoSubprocesso.NAO_INICIADO, processo.getDataLimite());
            Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);

            movimentacaoRepo.save(new Movimentacao(subprocessoSalvo, null, unidade, "Processo de revisão iniciado"));
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

        if (processo.getSituacao() != SituacaoProcesso.EM_ANDAMENTO) {
            throw new IllegalStateException("Apenas processos 'EM ANDAMENTO' podem ser finalizados.");
        }

        validarTodosSubprocessosHomologados(processo);
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
        for (Long codigoUnidade : codigosUnidades) {
            Unidade unidade = unidadeRepo.findById(codigoUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade com código " + codigoUnidade + " não foi encontrada ao validar mapas vigentes."));

            boolean hasMapaVigente = unidadeMapaRepo.findByUnidadeCodigo(codigoUnidade)
                .map(UnidadeMapa::getMapaVigenteCodigo).isPresent();

            if (!hasMapaVigente) {
                throw new ErroProcesso(String.format(
                    "A unidade %s não possui mapa vigente e não pode participar de um processo de revisão.", unidade.getSigla()
                ));
            }
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
                "Não é possível encerrar o processo. Unidades pendentes de homologação:\n- %s",
                String.join("\n- ", pendentes)
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
        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigo(processo.getCodigo());

        for (Subprocesso subprocesso : subprocessos) {
            try {
                Unidade unidade = Optional.ofNullable(subprocesso.getUnidade())
                    .orElseThrow(() -> new IllegalStateException("Subprocesso sem unidade."));

                ResponsavelDto responsavel = sgrhService.buscarResponsavelUnidade(unidade.getCodigo())
                    .orElseThrow(() -> new IllegalStateException("Unidade sem responsável."));

                UsuarioDto titular = sgrhService.buscarUsuarioPorTitulo(responsavel.titularTitulo())
                    .orElseThrow(() -> new IllegalStateException("Responsável sem dados de usuário."));

                String emailTitular = Optional.ofNullable(titular.email())
                    .filter(e -> !e.isBlank())
                    .orElseThrow(() -> new IllegalStateException("Usuário titular sem e-mail."));

                String mensagem = criarMensagemPersonalizada(unidade.getTipo().name());
                String html = notificacaoTemplateEmailService.criarEmailDeProcessoFinalizadoPorUnidade(
                    unidade.getSigla(), processo.getDescricao(), mensagem);

                servicoNotificacaoEmail.enviarEmailHtml(emailTitular, "SGC: Conclusão do Processo " + processo.getDescricao(), html);
                log.debug("E-mail de finalização enviado para a unidade {} ({})", unidade.getSigla(), emailTitular);

            } catch (Exception ex) {
                log.error("Falha ao enviar notificação de finalização para subprocesso {}: {}", subprocesso.getCodigo(), ex.getMessage(), ex);
            }
        }
    }

    private String criarMensagemPersonalizada(String tipoUnidade) {
        return switch (tipoUnidade) {
            case "OPERACIONAL" -> "Seu mapa de competências está agora vigente e pode ser visualizado através do sistema.";
            case "INTERMEDIARIA" -> "Os mapas de competências das unidades subordinadas a esta unidade estão agora vigentes e podem ser visualizados através do sistema.";
            case "INTEROPERACIONAL" -> "Seu mapa de competências e os mapas das unidades subordinadas estão agora vigentes e podem ser visualizados através do sistema.";
            default -> "O mapa de competências da unidade está agora vigente.";
        };
    }
}

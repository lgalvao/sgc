package sgc.processo;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.*;
import sgc.notificacao.ServicoNotificacaoEmail;
import sgc.notificacao.ServicoDeTemplateDeEmail;
import sgc.processo.dto.ProcessoDTO;
import sgc.processo.dto.ProcessoDetalheDTO;
import sgc.processo.dto.ReqAtualizarProcesso;
import sgc.processo.dto.ReqCriarProcesso;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.Movimentacao;
import sgc.subprocesso.MovimentacaoRepository;
import sgc.subprocesso.Subprocesso;
import sgc.subprocesso.SubprocessoRepository;
import sgc.unidade.Unidade;
import sgc.unidade.UnidadeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessoService {
    private final ProcessoRepository processoRepository;
    private final UnidadeRepository unidadeRepository;
    private final UnidadeProcessoRepository unidadeProcessoRepository;
    private final SubprocessoRepository subprocessoRepository;
    private final MapaRepository mapaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final UnidadeMapaRepository unidadeMapaRepository;
    private final CopiaMapaService servicoDeCopiaDeMapa;
    private final ApplicationEventPublisher publicadorDeEventos;
    private final ServicoNotificacaoEmail servicoNotificacaoEmail;
    private final ServicoDeTemplateDeEmail servicoDeTemplateDeEmail;
    private final SgrhService sgrhService;
    private final ProcessoMapper processoMapper;
    private final ProcessoDetalheMapper processoDetalheMapper;

    @Transactional
    public ProcessoDTO criar(ReqCriarProcesso requisicao) {
        if (requisicao.getDescricao() == null || requisicao.getDescricao().isBlank()) {
            throw new ConstraintViolationException("Preencha a descrição", null);
        }
        if (requisicao.getUnidades() == null || requisicao.getUnidades().isEmpty()) {
            throw new ConstraintViolationException("Pelo menos uma unidade participante deve ser incluída.", null);
        }

        if ("REVISAO".equalsIgnoreCase(requisicao.getTipo()) || "DIAGNOSTICO".equalsIgnoreCase(requisicao.getTipo())) {
            for (Long codigoUnidade : requisicao.getUnidades()) {
                if (unidadeRepository.findById(codigoUnidade).isEmpty()) {
                    throw new IllegalArgumentException("Unidade " + codigoUnidade + " não encontrada.");
                }
            }
        }

        Processo processo = new Processo();
        processo.setDescricao(requisicao.getDescricao());
        processo.setTipo(requisicao.getTipo());
        processo.setDataLimite(requisicao.getDataLimiteEtapa1());
        processo.setSituacao("CRIADO");
        processo.setDataCriacao(LocalDateTime.now());

        Processo processoSalvo = processoRepository.save(processo);

        publicadorDeEventos.publishEvent(new EventoDeProcessoCriado(this, processoSalvo.getCodigo()));

        return processoMapper.toDTO(processoSalvo);
    }

    @Transactional
    public ProcessoDTO atualizar(Long id, ReqAtualizarProcesso requisicao) {
        Processo processoAtualizado = processoRepository.findById(id)
                .map(processoExistente -> {
                    if (processoExistente.getSituacao() != null && !"CRIADO".equalsIgnoreCase(processoExistente.getSituacao())) {
                        throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser editados.");
                    }
                    processoExistente.setDescricao(requisicao.getDescricao());
                    processoExistente.setTipo(requisicao.getTipo());
                    processoExistente.setDataLimite(requisicao.getDataLimiteEtapa1());
                    return processoRepository.save(processoExistente);
                })
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado: " + id));
        return processoMapper.toDTO(processoAtualizado);
    }

    @Transactional
    public void apagar(Long id) {
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado: " + id));
        if (processo.getSituacao() != null && !"CRIADO".equalsIgnoreCase(processo.getSituacao())) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser removidos.");
        }
        processoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ProcessoDTO> obterPorId(Long id) {
        return processoRepository.findById(id).map(processoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public ProcessoDetalheDTO obterDetalhes(Long idProcesso, String perfil, Long idUnidadeUsuario) {
        if (perfil == null) {
            throw new ErroDominioAccessoNegado("Perfil inválido para acesso aos detalhes do processo.");
        }

        Processo processo = processoRepository.findById(idProcesso)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado: " + idProcesso));

        List<UnidadeProcesso> listaUnidadesProcesso = unidadeProcessoRepository.findByProcessoCodigo(idProcesso);
        List<Subprocesso> subprocessos = subprocessoRepository.findByProcessoCodigoWithUnidade(idProcesso);

        if ("GESTOR".equalsIgnoreCase(perfil)) {
            boolean unidadePresenteNoProcesso = false;
            if (subprocessos != null) {
                for (Subprocesso sp : subprocessos) {
                    if (sp.getUnidade() != null && idUnidadeUsuario != null && idUnidadeUsuario.equals(sp.getUnidade().getCodigo())) {
                        unidadePresenteNoProcesso = true;
                        break;
                    }
                }
            }
            if (!unidadePresenteNoProcesso) {
                throw new ErroDominioAccessoNegado("Usuário sem permissão para visualizar este processo.");
            }
        } else if (!"ADMIN".equalsIgnoreCase(perfil) && !"GESTOR".equalsIgnoreCase(perfil)) {
            throw new ErroDominioAccessoNegado("Perfil sem permissão.");
        }

        return processoDetalheMapper.toDetailDTO(processo, listaUnidadesProcesso, subprocessos);
    }

    @Transactional
    public ProcessoDTO iniciarProcessoMapeamento(Long id, List<Long> codigosUnidades) {
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado: " + id));

        if (processo.getSituacao() == null || !"CRIADO".equalsIgnoreCase(processo.getSituacao())) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            throw new IllegalArgumentException("A lista de unidades é obrigatória para iniciar o processo.");
        }

        validarUnidadesNaoEmProcessosAtivos(codigosUnidades);

        for (Long codigoUnidade : codigosUnidades) {
            Unidade unidade = unidadeRepository.findById(codigoUnidade)
                    .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada: " + codigoUnidade));

            UnidadeProcesso unidadeProcesso = criarSnapshotUnidadeProcesso(processo, unidade);
            unidadeProcessoRepository.save(unidadeProcesso);

            Mapa mapa = new Mapa();
            Mapa mapaSalvo = mapaRepository.save(mapa);

            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setProcesso(processo);
            subprocesso.setUnidade(unidade);
            subprocesso.setMapa(mapaSalvo);
            subprocesso.setSituacaoId("PENDENTE");
            subprocesso.setDataLimiteEtapa1(processo.getDataLimite());
            Subprocesso subprocessoSalvo = subprocessoRepository.save(subprocesso);

            Movimentacao movimentacao = new Movimentacao();
            movimentacao.setSubprocesso(subprocessoSalvo);
            movimentacao.setDataHora(LocalDateTime.now());
            movimentacao.setUnidadeOrigem(null);
            movimentacao.setUnidadeDestino(unidade);
            movimentacao.setDescricao("Processo iniciado");
            movimentacaoRepository.save(movimentacao);
        }

        processo.setSituacao("EM_ANDAMENTO");
        Processo processoSalvo = processoRepository.save(processo);

        publicadorDeEventos.publishEvent(new EventoDeProcessoIniciado(
                processoSalvo.getCodigo(),
                processoSalvo.getTipo(),
                LocalDateTime.now(),
                codigosUnidades
        ));

        return processoMapper.toDTO(processoSalvo);
    }

    private void validarUnidadesNaoEmProcessosAtivos(List<Long> codigosDasUnidades) {
        List<Processo> processosEmAndamento = processoRepository.findBySituacao("EM_ANDAMENTO");

        for (Long codigoUnidade : codigosDasUnidades) {
            Unidade unidade = unidadeRepository.findById(codigoUnidade)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unidade não encontrada: " + codigoUnidade));

            for (Processo processoEmAndamento : processosEmAndamento) {
                List<UnidadeProcesso> unidadesDoProcesso = unidadeProcessoRepository
                        .findByProcessoCodigo(processoEmAndamento.getCodigo());

                for (UnidadeProcesso up : unidadesDoProcesso) {
                    if (unidade.getSigla().equals(up.getSigla())) {
                        throw new IllegalStateException(String.format(
                                "A unidade %s já está participando do processo ativo: %s (código %d)",
                                unidade.getSigla(),
                                processoEmAndamento.getDescricao(),
                                processoEmAndamento.getCodigo()
                        ));
                    }
                }
            }
        }
    }

    private static UnidadeProcesso criarSnapshotUnidadeProcesso(Processo processo, Unidade unidade) {
        UnidadeProcesso up = new UnidadeProcesso();
        up.setProcessoCodigo(processo.getCodigo());
        up.setNome(unidade.getNome());
        up.setSigla(unidade.getSigla());
        up.setTitularTitulo(unidade.getTitular() != null ? unidade.getTitular().getTitulo() : null);
        up.setTipo(unidade.getTipo());
        up.setSituacao("PENDENTE");
        up.setUnidadeSuperiorCodigo(unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null);
        return up;
    }

    @Transactional
    public ProcessoDTO iniciarProcessoRevisao(Long id, List<Long> codigosUnidades) {
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado: " + id));

        if (processo.getSituacao() == null || !"CRIADO".equalsIgnoreCase(processo.getSituacao())) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            throw new IllegalArgumentException("A lista de unidades é obrigatória para iniciar o processo.");
        }

        validarUnidadesNaoEmProcessosAtivos(codigosUnidades);
        validarUnidadesComMapasVigentes(codigosUnidades);

        for (Long codigoUnidade : codigosUnidades) {
            Unidade unidade = unidadeRepository.findById(codigoUnidade)
                    .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada: " + codigoUnidade));

            UnidadeMapa unidadeMapa = unidadeMapaRepository.findByUnidadeCodigo(codigoUnidade)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Nenhum mapa vigente encontrado para a unidade: " + codigoUnidade));

            Long idMapaOrigem = unidadeMapa.getMapaVigente() != null ?
                    unidadeMapa.getMapaVigente().getCodigo() : null;

            if (idMapaOrigem == null) {
                throw new IllegalArgumentException("Mapa vigente inválido para a unidade: " + codigoUnidade);
            }

            Mapa mapaNovo = servicoDeCopiaDeMapa.copiarMapaParaUnidade(idMapaOrigem, codigoUnidade);

            UnidadeProcesso unidadeProcesso = criarSnapshotUnidadeProcesso(processo, unidade);
            unidadeProcessoRepository.save(unidadeProcesso);

            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setProcesso(processo);
            subprocesso.setUnidade(unidade);
            subprocesso.setMapa(mapaNovo);
            subprocesso.setSituacaoId("PENDENTE");
            subprocesso.setDataLimiteEtapa1(processo.getDataLimite());
            Subprocesso subprocessoSalvo = subprocessoRepository.save(subprocesso);

            Movimentacao movimentacao = new Movimentacao();
            movimentacao.setSubprocesso(subprocessoSalvo);
            movimentacao.setDataHora(LocalDateTime.now());
            movimentacao.setUnidadeOrigem(null);
            movimentacao.setUnidadeDestino(unidade);
            movimentacao.setDescricao("Processo iniciado");
            movimentacaoRepository.save(movimentacao);
        }

        processo.setSituacao("EM_ANDAMENTO");
        Processo processoSalvo = processoRepository.save(processo);

        publicadorDeEventos.publishEvent(new EventoDeProcessoIniciado(
                processoSalvo.getCodigo(),
                processoSalvo.getTipo(),
                LocalDateTime.now(),
                codigosUnidades
        ));

        return processoMapper.toDTO(processoSalvo);
    }

    private void validarUnidadesComMapasVigentes(List<Long> codigosDasUnidades) {
        for (Long codigoUnidade : codigosDasUnidades) {
            Optional<Mapa> mapaVigenteOptional = mapaRepository.findMapaVigenteByUnidade(codigoUnidade);

            if (mapaVigenteOptional.isEmpty()) {
                Unidade unidade = unidadeRepository.findById(codigoUnidade)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Unidade não encontrada: " + codigoUnidade));

                throw new IllegalStateException(String.format(
                        "A unidade %s não possui mapa vigente. " +
                                "Apenas unidades com mapas podem participar de processos de revisão.",
                        unidade.getSigla()
                ));
            }
        }
    }

    @Transactional
    public ProcessoDTO finalizarProcesso(Long id) {
        log.info("Iniciando finalização do processo: código={}", id);

        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo não encontrado: " + id));

        if (processo.getSituacao() == null || !"EM_ANDAMENTO".equalsIgnoreCase(processo.getSituacao())) {
            throw new IllegalStateException("Apenas processos em andamento podem ser finalizados.");
        }

        validarTodosSubprocessosHomologados(processo);
        tornarMapasVigentes(processo);

        processo.setSituacao("FINALIZADO");
        processo.setDataFinalizacao(LocalDateTime.now());
        processo = processoRepository.save(processo);

        enviarNotificacoesDeFinalizacao(processo);

        publicadorDeEventos.publishEvent(new EventoDeProcessoFinalizado(this, processo.getCodigo()));

        log.info("Processo finalizado com sucesso: código={}", id);

        return processoMapper.toDTO(processo);
    }

    private void validarTodosSubprocessosHomologados(Processo processo) {
        log.debug("Validando homologação de subprocessos do processo {}", processo.getCodigo());

        List<Subprocesso> subprocessos = subprocessoRepository
                .findByProcessoCodigo(processo.getCodigo());

        List<String> listaSubprocessosPendentes = obterListaDeSubprocessosPendentes(subprocessos);

        if (!listaSubprocessosPendentes.isEmpty()) {
            String mensagem = String.format("""
                            Não é possível encerrar o processo enquanto houver unidades com mapa de competência \
                            ainda não homologado.
                            
                            Unidades pendentes:
                            - %s
                            
                            Todos os subprocessos devem estar na situação 'MAPA_HOMOLOGADO'.""",
                    String.join("\n- ", listaSubprocessosPendentes)
            );

            log.warn("Validação falhou: {} subprocessos não homologados", listaSubprocessosPendentes.size());
            throw new ErroProcesso(mensagem);
        }

        log.info("Validação OK: {} subprocessos homologados", subprocessos.size());
    }

    private static List<String> obterListaDeSubprocessosPendentes(List<Subprocesso> subprocessos) {
        List<String> listaSubprocessosPendentes = new ArrayList<>();

        for (Subprocesso subprocesso : subprocessos) {
            if (!"MAPA_HOMOLOGADO".equalsIgnoreCase(subprocesso.getSituacaoId())) {
                String nomeUnidade = subprocesso.getUnidade() != null ?
                        subprocesso.getUnidade().getSigla() : "Unidade " + subprocesso.getCodigo();

                listaSubprocessosPendentes.add(
                        nomeUnidade + " (Situação: " + subprocesso.getSituacaoId() + ")"
                );
            }
        }
        return listaSubprocessosPendentes;
    }

    private void tornarMapasVigentes(Processo processo) {
        log.info("Tornando mapas vigentes para o processo {}", processo.getCodigo());

        List<Subprocesso> subprocessos = subprocessoRepository
                .findByProcessoCodigo(processo.getCodigo());

        int totalMapasAtualizados = 0;
        int totalMapasCriados = 0;

        for (Subprocesso subprocesso : subprocessos) {
            Long codigoUnidade = subprocesso.getUnidade() != null ?
                    subprocesso.getUnidade().getCodigo() : null;

            if (codigoUnidade == null) {
                log.warn("Subprocesso {} sem unidade associada. Pulando.", subprocesso.getCodigo());
                continue;
            }

            Mapa mapaDoSubprocesso = subprocesso.getMapa();

            if (mapaDoSubprocesso == null) {
                log.error("Subprocesso {} sem mapa associado.", subprocesso.getCodigo());
                throw new ErroEntidadeNaoEncontrada(
                        "Mapa não encontrado para o subprocesso " + subprocesso.getCodigo()
                );
            }

            Optional<UnidadeMapa> unidadeMapaExistenteOptional = unidadeMapaRepository
                    .findByUnidadeCodigo(codigoUnidade);

            if (unidadeMapaExistenteOptional.isPresent()) {
                UnidadeMapa unidadeMapa = unidadeMapaExistenteOptional.get();
                unidadeMapa.setMapaVigenteCodigo(mapaDoSubprocesso.getCodigo());
                unidadeMapa.setDataVigencia(LocalDate.now());
                unidadeMapaRepository.save(unidadeMapa);

                totalMapasAtualizados++;
                log.debug("Mapa vigente ATUALIZADO: unidade={}, novoMapa={}",
                        codigoUnidade, mapaDoSubprocesso.getCodigo());
            } else {
                UnidadeMapa unidadeMapa = new UnidadeMapa();
                unidadeMapa.setUnidadeCodigo(codigoUnidade);
                unidadeMapa.setMapaVigenteCodigo(mapaDoSubprocesso.getCodigo());
                unidadeMapa.setDataVigencia(LocalDate.now());
                unidadeMapaRepository.save(unidadeMapa);

                totalMapasCriados++;
                log.debug("Mapa vigente CRIADO: unidade={}, mapa={}",
                        codigoUnidade, mapaDoSubprocesso.getCodigo());
            }
        }

        log.info("Mapas vigentes processados: {} atualizados, {} criados, total={}",
                totalMapasAtualizados, totalMapasCriados, subprocessos.size());
    }

    private void enviarNotificacoesDeFinalizacao(Processo processo) {
        log.info("Enviando notificações de finalização para o processo {}", processo.getCodigo());

        List<Subprocesso> subprocessos = subprocessoRepository
                .findByProcessoCodigo(processo.getCodigo());

        int totalNotificacoesEnviadas = 0;
        int totalFalhas = 0;

        for (Subprocesso subprocesso : subprocessos) {
            try {
                Long codigoUnidade = subprocesso.getUnidade() != null ?
                        subprocesso.getUnidade().getCodigo() : null;

                if (codigoUnidade == null) {
                    log.warn("Subprocesso {} sem unidade. Pulando notificação.", subprocesso.getCodigo());
                    continue;
                }

                Optional<ResponsavelDto> responsavelOptional = sgrhService
                        .buscarResponsavelUnidade(codigoUnidade);

                if (responsavelOptional.isEmpty() || responsavelOptional.get().titularTitulo() == null) {
                    log.warn("Unidade {} sem responsável. Pulando notificação.", codigoUnidade);
                    continue;
                }

                ResponsavelDto responsavel = responsavelOptional.get();

                Optional<UsuarioDto> titularOptional = sgrhService
                        .buscarUsuarioPorTitulo(responsavel.titularTitulo());

                if (titularOptional.isEmpty() || titularOptional.get().email() == null) {
                    log.warn("Titular da unidade {} sem e-mail. Pulando notificação.", codigoUnidade);
                    continue;
                }

                UsuarioDto titular = titularOptional.get();

                UnidadeDto unidade = sgrhService.buscarUnidadePorCodigo(codigoUnidade)
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                                "Unidade não encontrada: " + codigoUnidade));

                String mensagemPersonalizada = criarMensagemPersonalizada(unidade);

                String htmlEmail = servicoDeTemplateDeEmail.criarEmailDeProcessoFinalizadoPorUnidade(
                        unidade.sigla(),
                        processo.getDescricao(),
                        mensagemPersonalizada
                );

                servicoNotificacaoEmail.enviarEmailHtml(
                        titular.email(),
                        "SGC: Conclusão do processo " + processo.getDescricao(),
                        htmlEmail
                );

                totalNotificacoesEnviadas++;
                log.debug("E-mail de finalização enviado para a unidade {} ({})",
                        unidade.sigla(), titular.email());

            } catch (Exception ex) {
                totalFalhas++;
                log.error("Erro ao enviar notificação de finalização para o subprocesso {}: {}",
                        subprocesso.getCodigo(), ex.getMessage(), ex);
            }
        }

        log.info("Notificações de finalização: {} enviadas, {} falhas, total de {} subprocessos.",
                totalNotificacoesEnviadas, totalFalhas, subprocessos.size());
    }

    private static String criarMensagemPersonalizada(UnidadeDto unidade) {
        String mensagemPersonalizada;

        if ("OPERACIONAL".equalsIgnoreCase(unidade.tipo())) {
            mensagemPersonalizada = "Seu mapa de competências está agora vigente e pode ser " +
                    "visualizado através do sistema.";
        } else if ("INTERMEDIARIA".equalsIgnoreCase(unidade.tipo())) {
            mensagemPersonalizada = "Os mapas de competências das unidades subordinadas a esta " +
                    "unidade estão agora vigentes e podem ser visualizados através do sistema.";
        } else if ("INTEROPERACIONAL".equalsIgnoreCase(unidade.tipo())) {
            mensagemPersonalizada = "Seu mapa de competências e os mapas das unidades subordinadas " +
                    "estão agora vigentes e podem ser visualizados através do sistema.";
        } else {
            mensagemPersonalizada = "O mapa de competências da unidade está agora vigente.";
        }
        return mensagemPersonalizada;
    }

    public record EventoDeProcessoCriado(Object source, Long idProcesso) {
    }

    public record EventoDeProcessoIniciado(Long idProcesso, String tipo, LocalDateTime dataInicio, List<Long> idsUnidades) {
    }

    public record EventoDeProcessoFinalizado(Object source, Long idProcesso) {
    }
}
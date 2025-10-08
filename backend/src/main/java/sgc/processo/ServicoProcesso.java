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
import sgc.processo.dto.RequisicaoAtualizarProcesso;
import sgc.processo.dto.RequisicaoCriarProcesso;
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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServicoProcesso {
    private final RepositorioProcesso repositorioProcesso;
    private final UnidadeRepository unidadeRepository;
    private final UnidadeProcessoRepository unidadeProcessoRepository;
    private final SubprocessoRepository subprocessoRepository;
    private final MapaRepository mapaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final UnidadeMapaRepository unidadeMapaRepository;
    private final CopiaMapaServico servicoDeCopiaDeMapa;
    private final ApplicationEventPublisher publicadorDeEventos;
    private final ServicoNotificacaoEmail servicoNotificacaoEmail;
    private final ServicoDeTemplateDeEmail servicoDeTemplateDeEmail;
    private final SgrhService sgrhService;
    private final ProcessoMapper processoMapper;
    private final ProcessoDetalheMapper processoDetalheMapper;

    @Transactional
    public ProcessoDTO criar(RequisicaoCriarProcesso requisicao) {
        if (requisicao.getDescricao() == null || requisicao.getDescricao().isBlank()) {
            throw new ConstraintViolationException("A descrição do processo é obrigatória.", null);
        }
        if (requisicao.getUnidades() == null || requisicao.getUnidades().isEmpty()) {
            throw new ConstraintViolationException("Pelo menos uma unidade participante deve ser selecionada.", null);
        }

        if ("REVISAO".equalsIgnoreCase(requisicao.getTipo()) || "DIAGNOSTICO".equalsIgnoreCase(requisicao.getTipo())) {
            for (Long codigoUnidade : requisicao.getUnidades()) {
                if (unidadeRepository.findById(codigoUnidade).isEmpty()) {
                    throw new ErroEntidadeNaoEncontrada("Unidade com código " + codigoUnidade + " não foi encontrada.");
                }
            }
        }

        Processo processo = new Processo();
        processo.setDescricao(requisicao.getDescricao());
        processo.setTipo(requisicao.getTipo());
        processo.setDataLimite(requisicao.getDataLimiteEtapa1());
        processo.setSituacao("CRIADO");
        processo.setDataCriacao(LocalDateTime.now());

        Processo processoSalvo = repositorioProcesso.save(processo);

        publicadorDeEventos.publishEvent(new ProcessoCriadoEvento(this, processoSalvo.getCodigo()));
        log.info("Processo '{}' (código {}) criado com sucesso.", processoSalvo.getDescricao(), processoSalvo.getCodigo());

        return processoMapper.toDTO(processoSalvo);
    }

    @Transactional
    public ProcessoDTO atualizar(Long id, RequisicaoAtualizarProcesso requisicao) {
        Processo processo = repositorioProcesso.findById(id)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo não encontrado: " + id));

        if (!"CRIADO".equalsIgnoreCase(processo.getSituacao())) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser editados.");
        }

        processo.setDescricao(requisicao.getDescricao());
        processo.setTipo(requisicao.getTipo());
        processo.setDataLimite(requisicao.getDataLimiteEtapa1());

        Processo processoAtualizado = repositorioProcesso.save(processo);
        log.info("Processo {} atualizado com sucesso.", id);

        return processoMapper.toDTO(processoAtualizado);
    }

    @Transactional
    public void apagar(Long id) {
        Processo processo = repositorioProcesso.findById(id)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo não encontrado: " + id));

        if (!"CRIADO".equalsIgnoreCase(processo.getSituacao())) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser removidos.");
        }

        repositorioProcesso.deleteById(id);
        log.info("Processo {} removido com sucesso.", id);
    }

    @Transactional(readOnly = true)
    public Optional<ProcessoDTO> obterPorId(Long id) {
        return repositorioProcesso.findById(id).map(processoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public ProcessoDetalheDTO obterDetalhes(Long idProcesso, String perfil, Long idUnidadeUsuario) {
        if (perfil == null || perfil.isBlank()) {
            throw new ErroDominioAccessoNegado("Perfil inválido para acesso aos detalhes do processo.");
        }

        Processo processo = repositorioProcesso.findById(idProcesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo não encontrado: " + idProcesso));

        if ("GESTOR".equalsIgnoreCase(perfil)) {
            boolean unidadeDoUsuarioParticipa = subprocessoRepository.existsByProcessoCodigoAndUnidadeCodigo(idProcesso, idUnidadeUsuario);
            if (!unidadeDoUsuarioParticipa) {
                throw new ErroDominioAccessoNegado("Acesso negado. Sua unidade não participa deste processo.");
            }
        } else if (!"ADMIN".equalsIgnoreCase(perfil)) {
            throw new ErroDominioAccessoNegado("Acesso negado. Perfil sem permissão para ver detalhes do processo.");
        }

        List<UnidadeProcesso> listaUnidadesProcesso = unidadeProcessoRepository.findByProcessoCodigo(idProcesso);
        List<Subprocesso> subprocessos = subprocessoRepository.findByProcessoCodigoWithUnidade(idProcesso);

        return processoDetalheMapper.toDetailDTO(processo, listaUnidadesProcesso, subprocessos);
    }

    @Transactional
    public ProcessoDTO iniciarProcessoMapeamento(Long id, List<Long> codigosUnidades) {
        Processo processo = repositorioProcesso.findById(id)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo não encontrado: " + id));

        if (!"CRIADO".equalsIgnoreCase(processo.getSituacao())) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            throw new IllegalArgumentException("A lista de unidades é obrigatória para iniciar o processo de mapeamento.");
        }

        validarUnidadesNaoEmProcessosAtivos(codigosUnidades);

        for (Long codigoUnidade : codigosUnidades) {
            Unidade unidade = unidadeRepository.findById(codigoUnidade)
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade não encontrada: " + codigoUnidade));

            UnidadeProcesso unidadeProcesso = criarSnapshotUnidadeProcesso(processo, unidade);
            unidadeProcessoRepository.save(unidadeProcesso);

            Mapa mapa = mapaRepository.save(new Mapa());

            Subprocesso subprocesso = new Subprocesso(processo, unidade, mapa, "PENDENTE", processo.getDataLimite());
            Subprocesso subprocessoSalvo = subprocessoRepository.save(subprocesso);

            movimentacaoRepository.save(new Movimentacao(subprocessoSalvo, null, unidade, "Processo iniciado"));
        }

        processo.setSituacao("EM_ANDAMENTO");
        Processo processoSalvo = repositorioProcesso.save(processo);

        publicadorDeEventos.publishEvent(new ProcessoIniciadoEvento(
                processoSalvo.getCodigo(),
                processoSalvo.getTipo(),
                LocalDateTime.now(),
                codigosUnidades
        ));

        log.info("Processo de mapeamento {} iniciado para {} unidades.", id, codigosUnidades.size());
        return processoMapper.toDTO(processoSalvo);
    }

    @Transactional
    public ProcessoDTO iniciarProcessoRevisao(Long id, List<Long> codigosUnidades) {
        Processo processo = repositorioProcesso.findById(id)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo não encontrado: " + id));

        if (!"CRIADO".equalsIgnoreCase(processo.getSituacao())) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            throw new IllegalArgumentException("A lista de unidades é obrigatória para iniciar o processo de revisão.");
        }

        validarUnidadesNaoEmProcessosAtivos(codigosUnidades);
        validarUnidadesComMapasVigentes(codigosUnidades);

        for (Long codigoUnidade : codigosUnidades) {
            Unidade unidade = unidadeRepository.findById(codigoUnidade)
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade não encontrada: " + codigoUnidade));

            Long idMapaOrigem = unidadeMapaRepository.findByUnidadeCodigo(codigoUnidade)
                    .map(UnidadeMapa::getMapaVigenteCodigo)
                    .orElseThrow(() -> new ProcessoErro("Mapa vigente não encontrado para a unidade: " + codigoUnidade));

            Mapa mapaNovo = servicoDeCopiaDeMapa.copiarMapaParaUnidade(idMapaOrigem, codigoUnidade);

            UnidadeProcesso unidadeProcesso = criarSnapshotUnidadeProcesso(processo, unidade);
            unidadeProcessoRepository.save(unidadeProcesso);

            Subprocesso subprocesso = new Subprocesso(processo, unidade, mapaNovo, "PENDENTE", processo.getDataLimite());
            Subprocesso subprocessoSalvo = subprocessoRepository.save(subprocesso);

            movimentacaoRepository.save(new Movimentacao(subprocessoSalvo, null, unidade, "Processo de revisão iniciado"));
        }

        processo.setSituacao("EM_ANDAMENTO");
        Processo processoSalvo = repositorioProcesso.save(processo);

        publicadorDeEventos.publishEvent(new ProcessoIniciadoEvento(
                processoSalvo.getCodigo(),
                processoSalvo.getTipo(),
                LocalDateTime.now(),
                codigosUnidades
        ));

        log.info("Processo de revisão {} iniciado para {} unidades.", id, codigosUnidades.size());
        return processoMapper.toDTO(processoSalvo);
    }

    @Transactional
    public ProcessoDTO finalizar(Long id) {
        log.info("Iniciando finalização do processo: código={}", id);

        Processo processo = repositorioProcesso.findById(id)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo não encontrado: " + id));

        if (!"EM_ANDAMENTO".equalsIgnoreCase(processo.getSituacao())) {
            throw new IllegalStateException("Apenas processos 'EM ANDAMENTO' podem ser finalizados.");
        }

        validarTodosSubprocessosHomologados(processo);
        tornarMapasVigentes(processo);

        processo.setSituacao("FINALIZADO");
        processo.setDataFinalizacao(LocalDateTime.now());
        processo = repositorioProcesso.save(processo);

        enviarNotificacoesDeFinalizacao(processo);

        publicadorDeEventos.publishEvent(new ProcessoFinalizadoEvento(this, processo.getCodigo()));

        log.info("Processo finalizado com sucesso: código={}", id);
        return processoMapper.toDTO(processo);
    }

    private void validarUnidadesNaoEmProcessosAtivos(List<Long> codigosUnidades) {
        List<Long> unidadesEmProcesso = unidadeProcessoRepository.findUnidadesInProcessosAtivos(codigosUnidades);
        if (!unidadesEmProcesso.isEmpty()) {
            throw new ProcessoErro("As seguintes unidades já participam de outro processo ativo: " + unidadesEmProcesso);
        }
    }

    private void validarUnidadesComMapasVigentes(List<Long> codigosUnidades) {
        for (Long codigoUnidade : codigosUnidades) {
            boolean hasMapaVigente = unidadeMapaRepository.findByUnidadeCodigo(codigoUnidade)
                .map(UnidadeMapa::getMapaVigenteCodigo).isPresent();

            if (!hasMapaVigente) {
                Unidade unidade = unidadeRepository.findById(codigoUnidade).orElse(null);
                String sigla = unidade != null ? unidade.getSigla() : "Código " + codigoUnidade;
                throw new ProcessoErro(String.format(
                    "A unidade %s não possui mapa vigente e não pode participar de um processo de revisão.", sigla
                ));
            }
        }
    }

    private UnidadeProcesso criarSnapshotUnidadeProcesso(Processo processo, Unidade unidade) {
        return new UnidadeProcesso(
            processo.getCodigo(),
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
        List<Subprocesso> subprocessos = subprocessoRepository.findByProcessoCodigo(processo.getCodigo());

        List<String> pendentes = subprocessos.stream()
            .filter(sp -> !"MAPA_HOMOLOGADO".equalsIgnoreCase(sp.getSituacaoId()))
            .map(sp -> String.format("%s (Situação: %s)",
                sp.getUnidade() != null ? sp.getUnidade().getSigla() : "Subprocesso " + sp.getCodigo(),
                sp.getSituacaoId()))
            .toList();

        if (!pendentes.isEmpty()) {
            String mensagem = String.format(
                "Não é possível encerrar o processo. Unidades pendentes de homologação:\n- %s",
                String.join("\n- ", pendentes)
            );
            log.warn("Validação de finalização falhou: {} subprocessos não homologados.", pendentes.size());
            throw new ProcessoErro(mensagem);
        }
        log.info("Validação OK: {} subprocessos homologados.", subprocessos.size());
    }

    private void tornarMapasVigentes(Processo processo) {
        log.info("Tornando mapas vigentes para o processo {}", processo.getCodigo());
        List<Subprocesso> subprocessos = subprocessoRepository.findByProcessoCodigo(processo.getCodigo());

        for (Subprocesso subprocesso : subprocessos) {
            Long codigoUnidade = Optional.ofNullable(subprocesso.getUnidade()).map(Unidade::getCodigo)
                .orElseThrow(() -> new ProcessoErro("Subprocesso " + subprocesso.getCodigo() + " sem unidade associada."));

            Mapa mapaDoSubprocesso = Optional.ofNullable(subprocesso.getMapa())
                .orElseThrow(() -> new ProcessoErro("Subprocesso " + subprocesso.getCodigo() + " sem mapa associado."));

            UnidadeMapa unidadeMapa = unidadeMapaRepository.findByUnidadeCodigo(codigoUnidade)
                .orElse(new UnidadeMapa(codigoUnidade));

            unidadeMapa.setMapaVigenteCodigo(mapaDoSubprocesso.getCodigo());
            unidadeMapa.setDataVigencia(LocalDate.now());
            unidadeMapaRepository.save(unidadeMapa);

            log.debug("Mapa vigente para unidade {} definido como mapa {}", codigoUnidade, mapaDoSubprocesso.getCodigo());
        }
        log.info("Mapas de {} subprocessos foram definidos como vigentes.", subprocessos.size());
    }

    private void enviarNotificacoesDeFinalizacao(Processo processo) {
        log.info("Enviando notificações de finalização para o processo {}", processo.getCodigo());
        List<Subprocesso> subprocessos = subprocessoRepository.findByProcessoCodigo(processo.getCodigo());

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

                String mensagem = criarMensagemPersonalizada(unidade.getTipo());
                String html = servicoDeTemplateDeEmail.criarEmailDeProcessoFinalizadoPorUnidade(
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
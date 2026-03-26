package sgc.subprocesso.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.springframework.util.*;
import sgc.alerta.*;
import sgc.comum.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.time.format.*;
import java.util.*;

import static sgc.processo.model.TipoProcesso.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA;
import static sgc.subprocesso.model.TipoTransicao.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubprocessoTransicaoService {
    private final SubprocessoRepo subprocessoRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final AnaliseRepo analiseRepo;
    private final SubprocessoValidacaoService validacaoService;
    private final SubprocessoNotificacaoService notificacaoService;
    private final UnidadeService unidadeService;
    private final HierarquiaService hierarquiaService;
    private final UsuarioFacade usuarioFacade;
    private final ImpactoMapaService impactoMapaService;
    private final MapaManutencaoService mapaManutencaoService;
    private final EmailService emailService;
    private final AlertaFacade alertaService;

    private static final String SIGLA_ADMIN = "ADMIN";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String ETAPA_REVISAO = "revisão";
    private static final String ETAPA_CADASTRO = "cadastro";

    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_DISPONIBILIZADO = Map.of(
            MAPEAMENTO, MAPEAMENTO_MAPA_DISPONIBILIZADO,
            REVISAO, REVISAO_MAPA_DISPONIBILIZADO
    );
    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_COM_SUGESTOES = Map.of(
            MAPEAMENTO, MAPEAMENTO_MAPA_COM_SUGESTOES,
            REVISAO, REVISAO_MAPA_COM_SUGESTOES
    );
    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_VALIDADO = Map.of(
            MAPEAMENTO, MAPEAMENTO_MAPA_VALIDADO,
            REVISAO, REVISAO_MAPA_VALIDADO
    );
    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_HOMOLOGADO = Map.of(
            MAPEAMENTO, MAPEAMENTO_MAPA_HOMOLOGADO,
            REVISAO, REVISAO_MAPA_HOMOLOGADO
    );

    private Subprocesso buscarSubprocesso(Long codigo) {
        return subprocessoRepo.buscarPorCodigoComMapaEAtividades(codigo).orElseThrow();
    }

    private Unidade obterUnidadeLocalizacao(Subprocesso sp) {
        if (sp.getLocalizacaoAtual() != null) return sp.getLocalizacaoAtual();
        if (sp.getCodigo() == null) return sp.getUnidade();

        List<Movimentacao> movs = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
        if (movs.isEmpty()) return sp.getUnidade();

        Unidade destino = movs.getFirst().getUnidadeDestino();
        return (destino != null) ? destino : sp.getUnidade();
    }

    public void registrarTransicao(RegistrarTransicaoCommand cmd) {
        Usuario usuario = cmd.usuario();
        Subprocesso sp = cmd.sp();

        Movimentacao movimentacao = Movimentacao.builder()
                .subprocesso(sp)
                .unidadeOrigem(cmd.origem())
                .unidadeDestino(cmd.destino())
                .descricao(cmd.tipo().getDescMovimentacao())
                .usuario(usuario)
                .build();
        movimentacaoRepo.save(movimentacao);

        sp.setLocalizacaoAtual(cmd.destino());
        subprocessoRepo.save(sp);

        notificacaoService.notificarTransicao(NotificacaoCommand.builder()
                .subprocesso(sp)
                .tipoTransicao(cmd.tipo())
                .unidadeOrigem(cmd.origem())
                .unidadeDestino(cmd.destino())
                .observacoes(cmd.observacoes())
                .build());
    }

    public void registrarAnalise(RegistrarWorkflowCommand cmd) {
        Subprocesso sp = cmd.sp();
        Usuario usuario = cmd.usuario();

        CriarAnaliseRequest request = CriarAnaliseRequest.builder()
                .observacoes(cmd.observacoes())
                .acao(cmd.tipoAcaoAnalise())
                .motivo(cmd.motivoAnalise())
                .build();

        criarAnalise(sp, request, cmd.tipoAnalise(), usuario);

        sp.setSituacao(cmd.novaSituacao());

        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(cmd.tipoTransicao())
                .origem(cmd.unidadeOrigemTransicao())
                .destino(cmd.unidadeDestinoTransicao())
                .usuario(usuario)
                .observacoes(cmd.observacoes())
                .build());
    }

    public Analise criarAnalise(Subprocesso sp, CriarAnaliseRequest request, TipoAnalise tipo, Usuario usuario) {
        Analise analise = Analise.builder()
                .subprocesso(sp)
                .dataHora(LocalDateTime.now())
                .observacoes(request.observacoes())
                .tipo(tipo)
                .acao(request.acao())
                .unidadeCodigo(usuario.getUnidadeAtivaCodigo())
                .usuarioTitulo(usuario.getTituloEleitoral())
                .motivo(request.motivo())
                .build();

        return analiseRepo.save(analise);
    }

    @Transactional
    public void disponibilizarCadastro(Long codSubprocesso, Usuario usuario) {
        log.info("Disponibilizando cadastro do subprocesso {}", codSubprocesso);
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp, MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        disponibilizar(sp, MAPEAMENTO_CADASTRO_DISPONIBILIZADO, TipoTransicao.CADASTRO_DISPONIBILIZADO, usuario);
    }

    public void disponibilizarRevisao(Long codSubprocesso, Usuario usuario) {
        log.info("Disponibilizando revisão do subprocesso {}", codSubprocesso);
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp, NAO_INICIADO, REVISAO_CADASTRO_EM_ANDAMENTO);

        if (sp.getSituacao() == NAO_INICIADO) {
            sp.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            subprocessoRepo.save(sp);
        }

        validacaoService.validarSituacaoPermitida(sp, REVISAO_CADASTRO_EM_ANDAMENTO);
        disponibilizar(sp, REVISAO_CADASTRO_DISPONIBILIZADA, TipoTransicao.REVISAO_CADASTRO_DISPONIBILIZADA, usuario);
    }

    private void disponibilizar(Subprocesso sp, SituacaoSubprocesso novaSituacao,
                                TipoTransicao transicao, Usuario usuario) {

        Mapa mapa = sp.getMapa();
        List<Atividade> atividades = mapaManutencaoService.atividadesMapaCodigoComConhecimentos(mapa.getCodigo());
        List<Atividade> atividadesSemConhecimento = atividades.isEmpty() ? List.of() : atividades.stream().filter(a -> a.getConhecimentos().isEmpty()).toList();

        validacaoService.validarRequisitosNegocioParaDisponibilizacao(sp, atividadesSemConhecimento);

        Unidade origem = sp.getUnidade();
        Unidade destino = origem.getUnidadeSuperior();
        if (destino == null) {
            log.info("Unidade {} não possui superior. Encaminhando para unidade central ADMIN.", origem.getSigla());
            destino = unidadeService.buscarPorSigla(SIGLA_ADMIN);
        }

        sp.setSituacao(novaSituacao);
        sp.setDataFimEtapa1(LocalDateTime.now());

        final Unidade destinoFinal = destino;
        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(transicao)
                .origem(origem)
                .destino(destinoFinal)
                .usuario(usuario)
                .build());
    }

    @Transactional
    public void disponibilizarMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        executarDisponibilizacaoMapa(codSubprocesso, request, usuario);
    }

    @Transactional
    public void disponibilizarMapaEmBloco(List<Long> subprocessoCodigos, DisponibilizarMapaRequest request, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarDisponibilizacaoMapa(codSubprocesso, request, usuario));
    }

    private void executarDisponibilizacaoMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        log.info("Disponibilizando mapa do subprocesso {}", codSubprocesso);
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        validacaoService.validarSituacaoPermitida(sp,
                MAPEAMENTO_CADASTRO_HOMOLOGADO,
                MAPEAMENTO_MAPA_CRIADO,
                MAPEAMENTO_MAPA_COM_SUGESTOES,
                REVISAO_CADASTRO_HOMOLOGADA,
                REVISAO_MAPA_AJUSTADO,
                REVISAO_MAPA_COM_SUGESTOES
        );


        validacaoService.validarMapaParaDisponibilizacao(sp);

        LocalDate ultimaDataLimite = obterUltimaDataLimite(sp);
        if (ultimaDataLimite != null && request.dataLimite().isBefore(ultimaDataLimite)) {
            throw new sgc.comum.erros.ErroValidacao(Mensagens.DATA_LIMITE_MAIOR_OU_IGUAL_ULTIMA_DATA_SUBPROCESSO);
        }

        Mapa mapa = sp.getMapa();
        validacaoService.validarAssociacoesMapa(mapa.getCodigo());
        mapa.setSugestoes(null);

        String observacoes = request.observacoes();
        if (StringUtils.hasText(observacoes)) {
            mapa.setSugestoes(observacoes);
        }

        sp.setSituacao(SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo()));

        sp.setDataLimiteEtapa2(request.dataLimite().atStartOfDay());
        sp.setDataFimEtapa1(LocalDateTime.now());

        Unidade unidadeRaiz = unidadeService.buscarPorSigla(SIGLA_ADMIN);
        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_DISPONIBILIZADO)
                .origem(unidadeRaiz)
                .destino(sp.getUnidade())
                .usuario(usuario)
                .observacoes(observacoes)
                .build());
    }

    private LocalDate obterUltimaDataLimite(Subprocesso sp) {
        LocalDateTime dataLimiteEtapa1 = sp.getDataLimiteEtapa1();
        LocalDateTime dataLimiteEtapa2 = sp.getDataLimiteEtapa2();

        if (dataLimiteEtapa1 == null && dataLimiteEtapa2 == null) {
            return null;
        }
        if (dataLimiteEtapa1 == null) {
            return dataLimiteEtapa2.toLocalDate();
        }
        if (dataLimiteEtapa2 == null) {
            return dataLimiteEtapa1.toLocalDate();
        }
        return dataLimiteEtapa1.isAfter(dataLimiteEtapa2) ? dataLimiteEtapa1.toLocalDate() : dataLimiteEtapa2.toLocalDate();
    }

    public void submeterMapaAjustado(Long codSubprocesso, SubmeterMapaAjustadoRequest request, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp, REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO);
        validacaoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

        sp.setSituacao(SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo()));
        sp.setDataFimEtapa1(LocalDateTime.now());

        if (request.dataLimiteEtapa2() != null) {
            sp.setDataLimiteEtapa2(request.dataLimiteEtapa2());
        }

        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_DISPONIBILIZADO)
                .origem(sp.getUnidade())
                .destino(sp.getUnidade())
                .usuario(usuario)
                .observacoes(request.justificativa())
                .build());
    }

    @Transactional
    public void apresentarSugestoes(Long codSubprocesso, @Nullable String sugestoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp,
                MAPEAMENTO_MAPA_DISPONIBILIZADO,
                REVISAO_MAPA_DISPONIBILIZADO);

        sp.getMapa().setSugestoes(sugestoes);
        sp.setSituacao(SITUACAO_MAPA_COM_SUGESTOES.get(sp.getProcesso().getTipo()));
        sp.setDataFimEtapa2(LocalDateTime.now());

        Unidade destino = sp.getUnidade().getUnidadeSuperior();
        if (destino == null) {
            destino = sp.getUnidade();
        }

        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_SUGESTOES_APRESENTADAS)
                .origem(sp.getUnidade())
                .destino(destino)
                .usuario(usuario)
                .observacoes(sugestoes)
                .build());

        log.info("Sugestões aresentadas para mapa do SP {}: {}", codSubprocesso, sugestoes);
    }

    @Transactional
    public void validarMapa(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp, MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO);

        sp.setSituacao(SITUACAO_MAPA_VALIDADO.get(sp.getProcesso().getTipo()));
        sp.setDataFimEtapa2(LocalDateTime.now());

        Unidade destino = sp.getUnidade().getUnidadeSuperior();
        if (destino == null) {
            destino = sp.getUnidade();
        }

        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_VALIDADO)
                .origem(sp.getUnidade())
                .destino(destino)
                .usuario(usuario)
                .build());

        log.info("Validado mapa do SP {}", codSubprocesso);
    }

    public void devolverValidacao(Long codSubprocesso, @Nullable String justificativa, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp,
                MAPEAMENTO_MAPA_COM_SUGESTOES,
                MAPEAMENTO_MAPA_VALIDADO,
                REVISAO_MAPA_COM_SUGESTOES,
                REVISAO_MAPA_VALIDADO);

        Unidade unidadeAnalise = obterUnidadeLocalizacao(sp);
        List<Movimentacao> movs = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());

        Unidade unidadeDevolucao = movs.stream()
                .filter(m -> Objects.equals(m.getUnidadeDestino().getCodigo(), unidadeAnalise.getCodigo()))
                .map(Movimentacao::getUnidadeOrigem)
                .filter(unidadeOrigem -> hierarquiaService.isSubordinada(unidadeOrigem, unidadeAnalise))
                .findFirst()
                .orElse(sp.getUnidade());

        SituacaoSubprocesso novaSituacao = SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo());
        sp.setDataFimEtapa2(null);

        RegistrarWorkflowCommand workflowCommand = RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(novaSituacao)
                .tipoTransicao(TipoTransicao.MAPA_VALIDACAO_DEVOLVIDA)
                .tipoAnalise(TipoAnalise.VALIDACAO)
                .tipoAcaoAnalise(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeOrigemTransicao(unidadeAnalise)
                .unidadeDestinoTransicao(unidadeDevolucao)
                .usuario(usuario)
                .motivoAnalise(justificativa)
                .observacoes(justificativa)
                .build();

        registrarAnalise(workflowCommand);
        log.info("Devolvida validação do mapa do SP {}", codSubprocesso);
    }

    public void aceitarValidacao(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        executarAceiteValidacao(codSubprocesso, observacoes, usuario);
    }

    public void aceitarValidacaoEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarAceiteValidacao(codSubprocesso, null, usuario));
    }

    private void executarAceiteValidacao(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp,
                MAPEAMENTO_MAPA_COM_SUGESTOES,
                MAPEAMENTO_MAPA_VALIDADO,
                REVISAO_MAPA_COM_SUGESTOES,
                REVISAO_MAPA_VALIDADO);

        Unidade unidadeAtual = obterUnidadeLocalizacao(sp);
        Unidade proximaUnidade = unidadeAtual.getUnidadeSuperior();

        if (proximaUnidade == null) {
            CriarAnaliseRequest request = CriarAnaliseRequest.builder()
                    .observacoes(observacoes)
                    .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                    .motivo("Aceite da validação")
                    .build();

            criarAnalise(sp, request, TipoAnalise.VALIDACAO, usuario);

            sp.setSituacao(SITUACAO_MAPA_HOMOLOGADO.get(sp.getProcesso().getTipo()));
            subprocessoRepo.save(sp);
        } else {
            SituacaoSubprocesso novaSituacao = sp.getSituacao();
            registrarAnalise(RegistrarWorkflowCommand.builder()
                    .sp(sp)
                    .novaSituacao(novaSituacao)
                    .tipoTransicao(TipoTransicao.MAPA_VALIDACAO_ACEITA)
                    .tipoAnalise(TipoAnalise.VALIDACAO)
                    .tipoAcaoAnalise(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                    .unidadeAnalise(unidadeAtual)
                    .unidadeOrigemTransicao(unidadeAtual)
                    .unidadeDestinoTransicao(proximaUnidade)
                    .usuario(usuario)
                    .motivoAnalise("Aceite da validação")
                    .observacoes(observacoes)
                    .build());
        }

        log.info("Validação aceita para mapa do SP {}", codSubprocesso);
    }

    public void homologarValidacao(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        executarHomologacaoValidacao(codSubprocesso, observacoes, usuario);
    }

    public void homologarValidacaoEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarHomologacaoValidacao(codSubprocesso, null, usuario));
    }

    private void executarHomologacaoValidacao(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        log.info("Homologando validação do mapa do subprocesso {}", codSubprocesso);
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp,
                MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO,
                REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO);

        sp.setSituacao(SITUACAO_MAPA_HOMOLOGADO.get(sp.getProcesso().getTipo()));

        Unidade admin = unidadeService.buscarPorSigla(SIGLA_ADMIN);
        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_HOMOLOGADO)
                .origem(admin)
                .destino(admin)
                .usuario(usuario)
                .observacoes(observacoes)
                .build());
    }

    public void devolverCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarDevolucao(codSubprocesso, usuario, observacoes, false);
    }

    public void devolverRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarDevolucao(codSubprocesso, usuario, observacoes, true);
    }

    private void executarDevolucao(Long codSubprocesso, Usuario usuario, @Nullable String observacoes, boolean isRevisao) {
        log.info("Devolvendo {} do subprocesso {}", isRevisao ? ETAPA_REVISAO : ETAPA_CADASTRO, codSubprocesso);
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        SituacaoSubprocesso situacaoAtual = isRevisao ? REVISAO_CADASTRO_DISPONIBILIZADA : MAPEAMENTO_CADASTRO_DISPONIBILIZADO;
        validacaoService.validarSituacaoPermitida(sp, situacaoAtual);

        Unidade unidadeAnalise = obterUnidadeLocalizacao(sp);
        List<Movimentacao> movs = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());

        Unidade unidadeDevolucao = movs.stream()
                .filter(m -> Objects.equals(m.getUnidadeDestino().getCodigo(), unidadeAnalise.getCodigo()))
                .map(Movimentacao::getUnidadeOrigem)
                .filter(unidadeOrigem -> hierarquiaService.isSubordinada(unidadeOrigem, unidadeAnalise))
                .findFirst()
                .orElse(sp.getUnidade());

        SituacaoSubprocesso novaSituacao = situacaoAtual;
        if (Objects.equals(unidadeDevolucao.getCodigo(), sp.getUnidade().getCodigo())) {
            novaSituacao = isRevisao ? REVISAO_CADASTRO_EM_ANDAMENTO : MAPEAMENTO_CADASTRO_EM_ANDAMENTO;
            sp.setDataFimEtapa1(null);
        }

        registrarAnalise(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(novaSituacao)
                .tipoTransicao(isRevisao ? TipoTransicao.REVISAO_CADASTRO_DEVOLVIDA : TipoTransicao.CADASTRO_DEVOLVIDO)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(isRevisao ? TipoAcaoAnalise.DEVOLUCAO_REVISAO : TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeOrigemTransicao(unidadeAnalise)
                .unidadeDestinoTransicao(unidadeDevolucao)
                .usuario(usuario)
                .motivoAnalise(observacoes)
                .observacoes(observacoes)
                .build());
    }

    public void aceitarCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarAceite(codSubprocesso, usuario, observacoes, false);
    }

    public void aceitarRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarAceite(codSubprocesso, usuario, observacoes, true);
    }

    @Transactional
    public void aceitarCadastroEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> {
            boolean isRevisao = REVISAO == buscarSubprocesso(codSubprocesso).getProcesso().getTipo();
            executarAceite(codSubprocesso, usuario, "Avaliação em bloco", isRevisao);
        });
    }

    private void executarAceite(Long codSubprocesso, Usuario usuario, @Nullable String observacoes, boolean isRevisao) {
        log.info("Aceitando {} do subprocesso {}", isRevisao ? ETAPA_REVISAO : ETAPA_CADASTRO, codSubprocesso);
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        SituacaoSubprocesso situacaoAtual = isRevisao ? REVISAO_CADASTRO_DISPONIBILIZADA : MAPEAMENTO_CADASTRO_DISPONIBILIZADO;
        validacaoService.validarSituacaoPermitida(sp, situacaoAtual);

        Unidade unidadeAtual = obterUnidadeLocalizacao(sp);
        Unidade unidadeDestino = unidadeAtual.getUnidadeSuperior();
        
        if (unidadeDestino == null) {
            log.info("Unidade {} não possui superior direto. Encaminhando para a unidade raiz ADMIN.", unidadeAtual.getSigla());
            unidadeDestino = unidadeService.buscarPorSigla(SIGLA_ADMIN);
        }

        registrarAnalise(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(situacaoAtual)
                .tipoTransicao(isRevisao ? TipoTransicao.REVISAO_CADASTRO_ACEITA : TipoTransicao.CADASTRO_ACEITO)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(isRevisao ? TipoAcaoAnalise.ACEITE_REVISAO : TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                .unidadeAnalise(unidadeAtual)
                .unidadeOrigemTransicao(unidadeAtual)
                .unidadeDestinoTransicao(unidadeDestino)
                .usuario(usuario)
                .motivoAnalise(observacoes)
                .observacoes(observacoes)
                .build());
    }

    public void homologarCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarHomologacao(codSubprocesso, usuario, observacoes, false);
    }

    public void homologarRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarHomologacao(codSubprocesso, usuario, observacoes, true);
    }

    public void homologarCadastroEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> {
            boolean isRevisao = REVISAO == buscarSubprocesso(codSubprocesso).getProcesso().getTipo();
            executarHomologacao(codSubprocesso, usuario, "Homologação em bloco", isRevisao);
        });
    }

    private void executarHomologacao(Long codSubprocesso, Usuario usuario, @Nullable String observacoes, boolean isRevisao) {
        log.info("Homologando {} do subprocesso {}", isRevisao ? ETAPA_REVISAO : ETAPA_CADASTRO, codSubprocesso);
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        SituacaoSubprocesso situacaoAtual = isRevisao ? REVISAO_CADASTRO_DISPONIBILIZADA : MAPEAMENTO_CADASTRO_DISPONIBILIZADO;
        validacaoService.validarSituacaoPermitida(sp, situacaoAtual);

        boolean deveHomologar = true;
        if (isRevisao) {
            var impactos = impactoMapaService.verificarImpactos(sp, usuario);
            if (!impactos.temImpactos()) {
                sp.setSituacao(REVISAO_MAPA_HOMOLOGADO);
                subprocessoRepo.save(sp);
                deveHomologar = false;
            }
        }

        if (deveHomologar) {
            Unidade admin = unidadeService.buscarPorSigla(SIGLA_ADMIN);
            sp.setSituacao(isRevisao ? REVISAO_CADASTRO_HOMOLOGADA : MAPEAMENTO_CADASTRO_HOMOLOGADO);

            registrarTransicao(RegistrarTransicaoCommand.builder()
                    .sp(sp)
                    .tipo(isRevisao ? TipoTransicao.REVISAO_CADASTRO_HOMOLOGADA : TipoTransicao.CADASTRO_HOMOLOGADO)
                    .origem(admin)
                    .destino(admin)
                    .usuario(usuario)
                    .observacoes(observacoes)
                    .build());
        }
    }

    public void reabrirCadastro(Long codigo, String justificativa) {
        executarReabertura(codigo, justificativa, MAPEAMENTO_MAPA_HOMOLOGADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                TipoTransicao.CADASTRO_REABERTO, false);
    }

    public void reabrirRevisaoCadastro(Long codigo, String justificativa) {
        executarReabertura(codigo,
                justificativa,
                REVISAO_MAPA_HOMOLOGADO,
                REVISAO_CADASTRO_EM_ANDAMENTO,
                REVISAO_CADASTRO_REABERTA,
                true
        );
    }

    private void executarReabertura(Long codigo, String justificativa, SituacaoSubprocesso situacaoMinima,
                                    SituacaoSubprocesso novaSituacao, TipoTransicao tipoTransicao, boolean isRevisao) {

        Subprocesso sp = buscarSubprocesso(codigo);
        validacaoService.validarSituacaoMinima(sp,
                situacaoMinima,
                Mensagens.ERRO_SUBPROCESSO_EM_FASE.formatted(isRevisao ? ETAPA_REVISAO : ETAPA_CADASTRO)
        );

        Unidade admin = unidadeService.buscarPorSigla(SIGLA_ADMIN);
        Usuario usuario = usuarioFacade.usuarioAutenticado();

        sp.setSituacao(novaSituacao);
        sp.setDataFimEtapa1(null);

        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(tipoTransicao)
                .origem(admin)
                .destino(sp.getUnidade())
                .usuario(usuario)
                .observacoes(justificativa)
                .build());

        enviarAlertasReabertura(sp, justificativa, isRevisao);
        log.info("Reaberto {} do SP {}", isRevisao ? ETAPA_REVISAO : ETAPA_CADASTRO, codigo);
    }

    private void enviarAlertasReabertura(Subprocesso sp, String justificativa, boolean isRevisao) {
        Processo processo = sp.getProcesso();
        Unidade unidade = sp.getUnidade();

        if (isRevisao) {
            alertaService.criarAlertaReaberturaRevisao(processo, unidade, justificativa);
        } else {
            alertaService.criarAlertaReaberturaCadastro(processo, unidade);
        }

        Unidade superior = unidade.getUnidadeSuperior();
        while (superior != null) {
            if (isRevisao) {
                alertaService.criarAlertaReaberturaRevisaoSuperior(processo, superior, unidade);
            } else {
                alertaService.criarAlertaReaberturaCadastroSuperior(processo, superior, unidade);
            }
            superior = superior.getUnidadeSuperior();
        }
    }

    public void alterarDataLimite(Long codSubprocesso, LocalDate novaDataLimite) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        SituacaoSubprocesso s = sp.getSituacao();
        String situacaoSp = s.name();
        LocalDate ultimaDataLimite = obterUltimaDataLimite(sp);

        if (ultimaDataLimite != null && novaDataLimite.isBefore(ultimaDataLimite)) {
            throw new sgc.comum.erros.ErroValidacao(Mensagens.DATA_LIMITE_MAIOR_OU_IGUAL_ULTIMA_DATA_SUBPROCESSO);
        }

        LocalDateTime dataLimiteEtapa1 = novaDataLimite.atStartOfDay();
        if (situacaoSp.contains("CADASTRO")) {
            sp.setDataLimiteEtapa1(dataLimiteEtapa1);
        } else if (situacaoSp.contains("MAPA")) {
            sp.setDataLimiteEtapa2(dataLimiteEtapa1);
        } else {
            sp.setDataLimiteEtapa1(dataLimiteEtapa1);
        }

        subprocessoRepo.save(sp);

        String novaDataStr = novaDataLimite.format(DATE_FORMATTER);
        String assunto = Mensagens.ASSUNTO_DATA_LIMITE_ALTERADA;
        String corpo = Mensagens.CORPO_DATA_LIMITE_ALTERADA
                .formatted(sp.getUnidade().getSigla(), sp.getProcesso().getDescricao(), novaDataStr);

        String emailDestino = notificacaoService.getEmailUnidade(sp.getUnidade());
        emailService.enviarEmail(emailDestino, assunto, corpo);

        int etapa = situacaoSp.contains("MAPA") ? 2 : 1;
        alertaService.criarAlertaAlteracaoDataLimite(sp.getProcesso(), sp.getUnidade(), novaDataStr, etapa);
    }


}

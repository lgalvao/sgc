package sgc.subprocesso.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.springframework.util.*;
import sgc.alerta.*;
import sgc.comum.*;
import sgc.comum.erros.*;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubprocessoTransicaoService {
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
    private final SubprocessoRepo subprocessoRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final AnaliseRepo analiseRepo;
    private final SubprocessoConsultaService consultaService;
    private final LocalizacaoSubprocessoService localizacaoSubprocessoService;
    private final SubprocessoValidacaoService validacaoService;
    private final SubprocessoNotificacaoService notificacaoService;
    private final SubprocessoFluxoContextoService fluxoContextoService;
    private final UnidadeService unidadeService;
    private final UsuarioAplicacaoService usuarioAplicacaoService;
    private final MapaManutencaoService mapaManutencaoService;
    private final AlertaAplicacaoService alertaAplicacaoService;

    private static @Nullable String normalizarTexto(@Nullable String texto) {
        if (!StringUtils.hasText(texto)) {
            return null;
        }
        return texto.trim();
    }

    public void registrarTransicao(RegistrarTransicaoCommand cmd) {
        persistirTransicao(cmd);
        registrarComunicacoesTransicao(cmd);
    }

    public void registrarTransicaoSemComunicacoes(RegistrarTransicaoCommand cmd) {
        persistirTransicao(cmd);
    }

    private void persistirTransicao(RegistrarTransicaoCommand cmd) {
        Subprocesso sp = cmd.sp();
        String desc = cmd.tipo().getDescMovimentacao();
        if (desc.contains("%s")) {
            desc = desc.formatted(sp.getUnidade().getSigla());
        }
        Movimentacao movimentacao = Movimentacao.builder()
                .subprocesso(sp)
                .unidadeOrigem(cmd.origem())
                .unidadeDestino(cmd.destino())
                .descricao(desc)
                .usuario(cmd.usuario())
                .build();
        movimentacaoRepo.save(movimentacao);

        subprocessoRepo.save(sp);
    }

    private void registrarComunicacoesTransicao(RegistrarTransicaoCommand cmd) {
        Subprocesso sp = cmd.sp();
        notificacaoService.registrarComunicacoesTransicao(NotificacaoCommand.builder()
                .subprocesso(sp)
                .tipoTransicao(cmd.tipo())
                .unidadeOrigem(cmd.origem())
                .unidadeDestino(cmd.destino())
                .observacoes(cmd.observacoes())
                .notificarSuperior(cmd.notificarSuperior())
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

        criarAnalise(sp, request, cmd.tipoAnalise());

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

    public void registrarAnaliseSemComunicacoes(RegistrarWorkflowCommand cmd) {
        Subprocesso sp = cmd.sp();
        Usuario usuario = cmd.usuario();

        CriarAnaliseRequest request = CriarAnaliseRequest.builder()
                .observacoes(cmd.observacoes())
                .acao(cmd.tipoAcaoAnalise())
                .motivo(cmd.motivoAnalise())
                .build();

        criarAnalise(sp, request, cmd.tipoAnalise());
        sp.setSituacao(cmd.novaSituacao());

        persistirTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(cmd.tipoTransicao())
                .origem(cmd.unidadeOrigemTransicao())
                .destino(cmd.unidadeDestinoTransicao())
                .usuario(usuario)
                .observacoes(cmd.observacoes())
                .build());
    }

    public Analise criarAnalise(Subprocesso sp, CriarAnaliseRequest request, TipoAnalise tipo) {
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
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
    public void disponibilizarMapa(Long codSubprocesso, DisponibilizarMapaRequest request) {
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        executarDisponibilizacaoMapa(codSubprocesso, request, usuario);
    }

    @Transactional
    public void disponibilizarMapaEmBloco(List<Long> subprocessoCodigos, DisponibilizarMapaRequest request) {
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        List<Subprocesso> subprocessos = subprocessoRepo.buscarPorCodigosComMapaEAtividades(subprocessoCodigos);
        subprocessos.forEach(subprocesso -> executarDisponibilizacaoMapa(subprocesso, request, usuario, false));
        notificacaoService.notificarDisponibilizacaoMapaEmBloco(subprocessos);
    }

    @Transactional
    public void disponibilizarMapaEmBloco(List<Subprocesso> subprocessos, DisponibilizarMapaRequest request, Usuario usuario) {
        subprocessos.forEach(subprocesso -> executarDisponibilizacaoMapa(subprocesso, request, usuario, false));
        notificacaoService.notificarDisponibilizacaoMapaEmBloco(subprocessos);
    }

    private void executarDisponibilizacaoMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        executarDisponibilizacaoMapa(sp, request, usuario, true);
    }

    private void executarDisponibilizacaoMapa(Subprocesso sp, DisponibilizarMapaRequest request, Usuario usuario, boolean enviarEmails) {
        Long codSubprocesso = sp.getCodigo();
        log.info("Disponibilizando mapa do subprocesso {}", codSubprocesso);

        validarLocalizacaoEscrita(sp, usuario);

        validacaoService.validarSituacaoPermitida(sp,
                MAPEAMENTO_CADASTRO_HOMOLOGADO,
                MAPEAMENTO_MAPA_CRIADO,
                MAPEAMENTO_MAPA_COM_SUGESTOES,
                REVISAO_CADASTRO_HOMOLOGADA,
                REVISAO_MAPA_AJUSTADO,
                REVISAO_MAPA_COM_SUGESTOES
        );


        validacaoService.validarMapaParaDisponibilizacao(sp);

        LocalDate dataFimEtapaAnterior = obterDataFimEtapaAnterior(sp);
        if (dataFimEtapaAnterior != null && !request.dataLimite().isAfter(dataFimEtapaAnterior)) {
            throw new sgc.comum.erros.ErroValidacao(Mensagens.DATA_LIMITE_MAIOR_QUE_FIM_ETAPA_ANTERIOR);
        }

        Mapa mapa = sp.getMapa();
        validacaoService.validarAssociacoesMapa(mapa.getCodigo());
        mapa.setSugestoes(null);

        String observacoes = request.observacoes();
        if (StringUtils.hasText(observacoes)) {
            mapa.setSugestoes(observacoes);
        }

        sp.setSituacao(obterSituacaoObrigatoria(SITUACAO_MAPA_DISPONIBILIZADO, sp, "disponibilização de mapa"));

        sp.setDataLimiteEtapa2(request.dataLimite().atStartOfDay());
        sp.setDataFimEtapa1(LocalDateTime.now());

        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_DISPONIBILIZADO)
                .origem(obterUnidadeAdmin())
                .destino(sp.getUnidade())
                .usuario(usuario)
                .observacoes(normalizarTexto(observacoes))
                .notificarSuperior(enviarEmails ? null : Boolean.FALSE)
                .build());
    }

    private @Nullable LocalDate obterDataFimEtapaAnterior(Subprocesso sp) {
        LocalDateTime dataFimEtapa1 = sp.getDataFimEtapa1();
        return dataFimEtapa1 == null ? null : dataFimEtapa1.toLocalDate();
    }

    public void submeterMapaAjustado(Long codSubprocesso, SubmeterMapaAjustadoRequest request) {
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validarLocalizacaoEscrita(sp, usuario);
        validacaoService.validarSituacaoPermitida(sp, REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO);
        validacaoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

        sp.setSituacao(obterSituacaoObrigatoria(SITUACAO_MAPA_DISPONIBILIZADO, sp, "submissão de mapa ajustado"));
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
    public void apresentarSugestoes(Long codSubprocesso, @Nullable String sugestoes) {
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validarLocalizacaoEscrita(sp, usuario);
        validacaoService.validarSituacaoPermitida(sp,
                MAPEAMENTO_MAPA_DISPONIBILIZADO,
                REVISAO_MAPA_DISPONIBILIZADO);

        Mapa mapa = sp.getMapa();
        mapa.setSugestoes(sugestoes);
        mapaManutencaoService.salvarMapa(mapa);

        sp.setSituacao(obterSituacaoObrigatoria(SITUACAO_MAPA_COM_SUGESTOES, sp, "apresentação de sugestões"));
        sp.setDataFimEtapa2(LocalDateTime.now());

        registrarTransicaoParaSuperiorDaUnidade(sp, TipoTransicao.MAPA_SUGESTOES_APRESENTADAS, usuario, normalizarTexto(sugestoes));

        log.info("Sugestões apresentadas para mapa do SP {}: {}", codSubprocesso, sugestoes);
    }

    @Transactional
    public void validarMapa(Long codSubprocesso) {
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validarLocalizacaoEscrita(sp, usuario);
        validacaoService.validarSituacaoPermitida(sp,
                MAPEAMENTO_MAPA_DISPONIBILIZADO,
                REVISAO_MAPA_DISPONIBILIZADO);

        sp.setSituacao(obterSituacaoObrigatoria(SITUACAO_MAPA_VALIDADO, sp, "validação de mapa"));
        sp.setDataFimEtapa2(LocalDateTime.now());

        registrarTransicaoParaSuperiorDaUnidade(sp, TipoTransicao.MAPA_VALIDADO, usuario, null);

        log.info("Validado mapa do SP {}", codSubprocesso);
    }

    public void devolverValidacao(Long codSubprocesso, @Nullable String justificativa) {
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validarLocalizacaoEscrita(sp, usuario);
        validarSituacaoPermitidaParaDevolucao(usuario, sp);

        Unidade unidadeAnalise = localizacaoSubprocessoService.obterLocalizacaoAtual(sp);
        Unidade unidadeDevolucao = fluxoContextoService.buscarUnidadeDevolucaoObrigatoria(sp, unidadeAnalise);

        SituacaoSubprocesso novaSituacao = sp.getSituacao();
        if (Objects.equals(unidadeDevolucao.getCodigo(), sp.getUnidade().getCodigo())) {
            novaSituacao = obterSituacaoObrigatoria(SITUACAO_MAPA_DISPONIBILIZADO, sp, "devolução de validação");
            sp.setDataFimEtapa2(null);
        }

        registrarWorkflowComDestino(RegistrarWorkflowAnaliseCommand.builder()
                .sp(sp)
                .novaSituacao(novaSituacao)
                .tipoTransicao(TipoTransicao.MAPA_VALIDACAO_DEVOLVIDA)
                .tipoAnalise(TipoAnalise.VALIDACAO)
                .tipoAcaoAnalise(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeDestino(unidadeDevolucao)
                .usuario(usuario)
                .motivoAnalise(normalizarTexto(justificativa))
                .observacoes(normalizarTexto(justificativa))
                .modoComunicacao(RegistrarWorkflowAnaliseCommand.ModoComunicacaoWorkflow.PADRAO)
                .build());
        log.info("Devolvida validação do mapa do SP {}", codSubprocesso);
    }

    private void validarSituacaoPermitidaParaDevolucao(Usuario usuario, Subprocesso sp) {
        if (usuario.getPerfilAtivo() == Perfil.ADMIN) {
            validacaoService.validarSituacaoPermitida(sp,
                    MAPEAMENTO_MAPA_COM_SUGESTOES,
                    REVISAO_MAPA_COM_SUGESTOES);
            return;
        }
        validacaoService.validarSituacaoPermitida(sp,
                MAPEAMENTO_MAPA_COM_SUGESTOES,
                MAPEAMENTO_MAPA_VALIDADO,
                REVISAO_MAPA_COM_SUGESTOES,
                REVISAO_MAPA_VALIDADO);
    }

    public void aceitarValidacao(Long codSubprocesso, @Nullable String observacoes) {
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        executarAceiteValidacao(sp, observacoes, usuario);
    }

    public void aceitarValidacaoEmBloco(List<Long> subprocessoCodigos) {
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        List<Subprocesso> subprocessos = subprocessoRepo.buscarPorCodigosComMapaEAtividades(subprocessoCodigos);
        subprocessos.forEach(sp -> executarAceiteValidacao(sp, null, usuario, false));
        notificacaoService.notificarAceiteValidacaoEmBloco(subprocessos);
    }

    private void executarAceiteValidacao(Subprocesso sp, @Nullable String observacoes, Usuario usuario) {
        executarAceiteValidacao(sp, observacoes, usuario, true);
    }

    private void executarAceiteValidacao(Subprocesso sp, @Nullable String observacoes, Usuario usuario, boolean enviarEmails) {
        validarLocalizacaoEscrita(sp, usuario);
        validacaoService.validarSituacaoPermitida(sp,
                MAPEAMENTO_MAPA_COM_SUGESTOES,
                MAPEAMENTO_MAPA_VALIDADO,
                REVISAO_MAPA_COM_SUGESTOES,
                REVISAO_MAPA_VALIDADO);

        SituacaoSubprocesso novaSituacao = sp.getSituacao();
        registrarWorkflowParaSuperiorAtual(RegistrarWorkflowAnaliseCommand.builder()
                .sp(sp)
                .novaSituacao(novaSituacao)
                .tipoTransicao(TipoTransicao.MAPA_VALIDACAO_ACEITA)
                .tipoAnalise(TipoAnalise.VALIDACAO)
                .tipoAcaoAnalise(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                .usuario(usuario)
                .motivoAnalise("Aceite da validação")
                .observacoes(normalizarTexto(observacoes))
                .modoComunicacao(enviarEmails
                        ? RegistrarWorkflowAnaliseCommand.ModoComunicacaoWorkflow.PADRAO
                        : RegistrarWorkflowAnaliseCommand.ModoComunicacaoWorkflow.SEM_COMUNICACOES)
                .build());

        log.info("Validação aceita para mapa do SP {}", sp.getCodigo());
    }

    public void homologarValidacao(Long codSubprocesso, @Nullable String observacoes) {
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validarLocalizacaoEscrita(sp, usuario);
        executarHomologacaoValidacao(sp, observacoes, usuario);
    }

    public void homologarValidacaoEmBloco(List<Long> subprocessoCodigos) {
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        List<Subprocesso> subprocessos = subprocessoRepo.buscarPorCodigosComMapaEAtividades(subprocessoCodigos);
        subprocessos.forEach(sp -> executarHomologacaoValidacao(sp, null, usuario, true));
    }

    private void executarHomologacaoValidacao(Subprocesso sp, @Nullable String observacoes, Usuario usuario) {
        executarHomologacaoValidacao(sp, observacoes, usuario, false);
    }

    private void executarHomologacaoValidacao(Subprocesso sp, @Nullable String observacoes, Usuario usuario, boolean notificarUnidade) {
        log.info("Homologando validação do mapa do subprocesso {}", sp.getCodigo());
        validacaoService.validarSituacaoPermitida(sp,
                MAPEAMENTO_MAPA_VALIDADO,
                REVISAO_MAPA_VALIDADO);

        sp.setSituacao(obterSituacaoObrigatoria(SITUACAO_MAPA_HOMOLOGADO, sp, "homologação de validação"));

        registrarTransicaoDentroDoAdmin(sp, TipoTransicao.MAPA_HOMOLOGADO, usuario, normalizarTexto(observacoes));
        if (notificarUnidade) {
            alertaAplicacaoService.criarAlertaTransicao(
                    sp.getProcesso(),
                    Mensagens.ALERTA_MAPA_HOMOLOGADO.formatted(sp.getUnidade().getSigla()),
                    obterUnidadeAdmin(),
                    sp.getUnidade()
            );
            notificacaoService.notificarHomologacaoMapa(sp);
        }
    }

    private void registrarTransicaoParaSuperiorDaUnidade(
            Subprocesso sp,
            TipoTransicao tipoTransicao,
            Usuario usuario,
            @Nullable String observacoes
    ) {
        Unidade unidade = sp.getUnidade();
        Unidade unidadeSuperior = fluxoContextoService.buscarSuperiorImediato(unidade.getCodigo());
        if (unidadeSuperior != null) {
            registrarTransicao(RegistrarTransicaoCommand.builder()
                    .sp(sp)
                    .tipo(tipoTransicao)
                    .origem(unidade)
                    .destino(unidadeSuperior)
                    .usuario(usuario)
                    .observacoes(observacoes)
                    .build());
        }
    }

    public void registrarWorkflowParaSuperiorAtual(RegistrarWorkflowAnaliseCommand cmd) {
        Unidade unidadeAtual = localizacaoSubprocessoService.obterLocalizacaoAtual(cmd.sp());
        Unidade unidadeDestino = fluxoContextoService.buscarSuperiorImediato(unidadeAtual.getCodigo());
        if (unidadeDestino != null) {
            registrarWorkflowComDestino(cmdComAnaliseEDestino(cmd, unidadeAtual, unidadeDestino));
        } else if (cmd.usuario().getPerfilAtivo() == Perfil.ADMIN) {
            registrarWorkflowComDestino(cmdComAnaliseEDestino(cmd, unidadeAtual, unidadeAtual));
        }
    }

    public void registrarWorkflowComDestino(RegistrarWorkflowAnaliseCommand cmd) {
        Unidade unidadeAnalise = Objects.requireNonNull(cmd.unidadeAnalise(), "Unidade de analise obrigatoria");
        Unidade unidadeDestino = Objects.requireNonNull(cmd.unidadeDestino(), "Unidade de destino obrigatoria");
        RegistrarWorkflowCommand comando = RegistrarWorkflowCommand.builder()
                .sp(cmd.sp())
                .novaSituacao(cmd.novaSituacao())
                .tipoTransicao(cmd.tipoTransicao())
                .tipoAnalise(cmd.tipoAnalise())
                .tipoAcaoAnalise(cmd.tipoAcaoAnalise())
                .unidadeAnalise(unidadeAnalise)
                .unidadeOrigemTransicao(unidadeAnalise)
                .unidadeDestinoTransicao(unidadeDestino)
                .usuario(cmd.usuario())
                .motivoAnalise(cmd.motivoAnalise())
                .observacoes(cmd.observacoes())
                .notificarSuperior(cmd.modoComunicacao() == RegistrarWorkflowAnaliseCommand.ModoComunicacaoWorkflow.SEM_COMUNICACOES ? Boolean.FALSE : null)
                .build();

        switch (cmd.modoComunicacao()) {
            case PADRAO -> registrarAnalise(comando);
            case SEM_COMUNICACOES -> registrarAnaliseSemComunicacoes(comando);
        }
    }

    public void registrarWorkflowDentroDoAdmin(RegistrarWorkflowAnaliseCommand cmd) {
        Unidade admin = obterUnidadeAdmin();
        registrarWorkflowComDestino(cmdComAnaliseEDestino(cmd, admin, admin));
    }

    private void validarLocalizacaoEscrita(Subprocesso sp, Usuario usuario) {
        Unidade localizacao = localizacaoSubprocessoService.obterLocalizacaoAtual(sp);
        if (!Objects.equals(usuario.getUnidadeAtivaCodigo(), localizacao.getCodigo())) {
            throw new ErroAcessoNegado("Operação não permitida: o subprocesso não está localizado na sua unidade ativa.");
        }
    }

    private RegistrarWorkflowAnaliseCommand cmdComAnaliseEDestino(
            RegistrarWorkflowAnaliseCommand cmd,
            Unidade unidadeAnalise,
            Unidade unidadeDestino
    ) {
        return RegistrarWorkflowAnaliseCommand.builder()
                .sp(cmd.sp())
                .novaSituacao(cmd.novaSituacao())
                .tipoTransicao(cmd.tipoTransicao())
                .tipoAnalise(cmd.tipoAnalise())
                .tipoAcaoAnalise(cmd.tipoAcaoAnalise())
                .unidadeAnalise(unidadeAnalise)
                .unidadeDestino(unidadeDestino)
                .usuario(cmd.usuario())
                .motivoAnalise(cmd.motivoAnalise())
                .observacoes(cmd.observacoes())
                .modoComunicacao(cmd.modoComunicacao())
                .build();
    }

    private Unidade obterUnidadeAdmin() {
        return unidadeService.buscarAdmin();
    }

    private void registrarTransicaoDentroDoAdmin(
            Subprocesso sp,
            TipoTransicao tipoTransicao,
            Usuario usuario,
            @Nullable String observacoes
    ) {
        Unidade admin = obterUnidadeAdmin();
        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(tipoTransicao)
                .origem(admin)
                .destino(admin)
                .usuario(usuario)
                .observacoes(observacoes)
                .notificarSuperior(null)
                .build());
    }

    public void alterarDataLimite(Long codSubprocesso, LocalDate novaDataLimite) {
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        SituacaoSubprocesso situacaoSp = sp.getSituacao();
        if (situacaoSp.ehEtapaMapa()) {
            LocalDate dataFimEtapaAnterior = obterDataFimEtapaAnterior(sp);
            if (dataFimEtapaAnterior != null && !novaDataLimite.isAfter(dataFimEtapaAnterior)) {
                throw new sgc.comum.erros.ErroValidacao(Mensagens.DATA_LIMITE_MAIOR_QUE_FIM_ETAPA_ANTERIOR);
            }
        }

        atualizarDataLimitePorSituacao(sp, situacaoSp, novaDataLimite.atStartOfDay());

        subprocessoRepo.save(sp);

        executarEfeitosDerivadosAlteracaoDataLimite(sp, novaDataLimite, situacaoSp);
    }

    private void atualizarDataLimitePorSituacao(Subprocesso sp, SituacaoSubprocesso situacaoSp, LocalDateTime novaDataLimiteInicioDoDia) {
        if (situacaoSp.ehEtapaMapa()) {
            sp.setDataLimiteEtapa2(novaDataLimiteInicioDoDia);
            return;
        }
        sp.setDataLimiteEtapa1(novaDataLimiteInicioDoDia);
    }

    private void executarEfeitosDerivadosAlteracaoDataLimite(Subprocesso sp, LocalDate novaDataLimite, SituacaoSubprocesso situacaoSp) {
        String novaDataFormatada = novaDataLimite.format(FORMATO_DATA);
        int etapa = obterEtapaPorSituacao(situacaoSp);
        notificarAlteracaoDataLimite(sp, novaDataFormatada, etapa);
    }

    private int obterEtapaPorSituacao(SituacaoSubprocesso situacaoSp) {
        return situacaoSp.ehEtapaMapa() ? 2 : 1;
    }

    private void notificarAlteracaoDataLimite(Subprocesso sp, String novaDataFormatada, int etapa) {
        notificacaoService.notificarAlteracaoDataLimite(sp, novaDataFormatada, etapa);
    }

    @SuppressWarnings("unused")
    private SituacaoSubprocesso obterSituacaoObrigatoria(Map<TipoProcesso, SituacaoSubprocesso> situacoes, Subprocesso subprocesso, String ignorado) {
        return situacoes.get(subprocesso.getProcesso().getTipo());
    }

}

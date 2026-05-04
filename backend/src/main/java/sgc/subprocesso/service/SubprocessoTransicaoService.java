package sgc.subprocesso.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.springframework.util.*;
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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
    private final UnidadeService unidadeService;
    private final HierarquiaService hierarquiaService;
    private final UnidadeHierarquiaService unidadeHierarquiaService;
    private final UsuarioFacade usuarioFacade;
    private final MapaManutencaoService mapaManutencaoService;

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

    private void persistirTransicao(RegistrarTransicaoCommand cmd) {
        Subprocesso sp = cmd.sp();
        Movimentacao movimentacao = Movimentacao.builder()
                .subprocesso(sp)
                .unidadeOrigem(cmd.origem())
                .unidadeDestino(cmd.destino())
                .descricao(cmd.tipo().getDescMovimentacao())
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

    public Analise criarAnalise(Subprocesso sp, CriarAnaliseRequest request, TipoAnalise tipo) {
        Usuario usuario = usuarioFacade.usuarioAutenticado();
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
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        executarDisponibilizacaoMapa(codSubprocesso, request, usuario);
    }

    @Transactional
    public void disponibilizarMapaEmBloco(List<Long> subprocessoCodigos, DisponibilizarMapaRequest request) {
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        subprocessoCodigos.forEach(codSubprocesso -> executarDisponibilizacaoMapa(codSubprocesso, request, usuario));
    }

    @Transactional
    public void disponibilizarMapaEmBloco(List<Subprocesso> subprocessos, DisponibilizarMapaRequest request, Usuario usuario) {
        subprocessos.forEach(subprocesso -> executarDisponibilizacaoMapa(subprocesso, request, usuario));
    }

    private void executarDisponibilizacaoMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        executarDisponibilizacaoMapa(sp, request, usuario);
    }

    private void executarDisponibilizacaoMapa(Subprocesso sp, DisponibilizarMapaRequest request, Usuario usuario) {
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

        sp.setSituacao(obterSituacaoObrigatoria(SITUACAO_MAPA_DISPONIBILIZADO, sp, "disponibilização de mapa"));

        sp.setDataLimiteEtapa2(request.dataLimite().atStartOfDay());
        sp.setDataFimEtapa1(LocalDateTime.now());

        registrarTransicaoDoAdminParaUnidade(sp, TipoTransicao.MAPA_DISPONIBILIZADO, usuario, normalizarTexto(observacoes));
    }

    private @Nullable LocalDate obterUltimaDataLimite(Subprocesso sp) {
        LocalDateTime dataLimiteEtapa1 = sp.getDataLimiteEtapa1();
        LocalDateTime dataLimiteEtapa2 = sp.getDataLimiteEtapa2();

        if (dataLimiteEtapa1 == null) {
            if (dataLimiteEtapa2 == null) {
                return null;
            }
            throw new IllegalStateException("Subprocesso %s com data limite da etapa 2 sem data limite da etapa 1".formatted(sp.getCodigo()));
        }
        if (dataLimiteEtapa2 == null) {
            return dataLimiteEtapa1.toLocalDate();
        }
        if (dataLimiteEtapa1.isAfter(dataLimiteEtapa2)) {
            throw new IllegalStateException("Subprocesso %s com data limite da etapa 1 posterior à etapa 2".formatted(sp.getCodigo()));
        }
        return dataLimiteEtapa2.toLocalDate();
    }

    public void submeterMapaAjustado(Long codSubprocesso, SubmeterMapaAjustadoRequest request) {
        Usuario usuario = usuarioFacade.usuarioAutenticado();
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
        Usuario usuario = usuarioFacade.usuarioAutenticado();
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
        Usuario usuario = usuarioFacade.usuarioAutenticado();
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
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validarLocalizacaoEscrita(sp, usuario);
        validarSituacaoPermitidaParaDevolucao(usuario, sp);

        Unidade unidadeAnalise = localizacaoSubprocessoService.obterLocalizacaoAtual(sp);
        Unidade unidadeDevolucao = obterUnidadeDevolucao(sp, unidadeAnalise);

        SituacaoSubprocesso novaSituacao = sp.getSituacao();
        if (Objects.equals(unidadeDevolucao.getCodigo(), sp.getUnidade().getCodigo())) {
            novaSituacao = obterSituacaoObrigatoria(SITUACAO_MAPA_DISPONIBILIZADO, sp, "devolução de validação");
            sp.setDataFimEtapa2(null);
        }

        registrarWorkflowComDestino(RegistrarWorkflowInternoCommand.devolucaoValidacao(
                sp,
                novaSituacao,
                unidadeAnalise,
                unidadeDevolucao,
                usuario,
                normalizarTexto(justificativa)
        ));
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
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        executarAceiteValidacao(sp, observacoes, usuario);
    }

    public void aceitarValidacaoEmBloco(List<Long> subprocessoCodigos) {
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        List<Subprocesso> subprocessos = subprocessoRepo.buscarPorCodigosComMapaEAtividades(subprocessoCodigos);
        subprocessos.forEach(sp -> executarAceiteValidacao(sp, null, usuario));
    }

    private void executarAceiteValidacao(Subprocesso sp, @Nullable String observacoes, Usuario usuario) {
        validarLocalizacaoEscrita(sp, usuario);
        validacaoService.validarSituacaoPermitida(sp,
                MAPEAMENTO_MAPA_COM_SUGESTOES,
                MAPEAMENTO_MAPA_VALIDADO,
                REVISAO_MAPA_COM_SUGESTOES,
                REVISAO_MAPA_VALIDADO);

        SituacaoSubprocesso novaSituacao = sp.getSituacao();
        registrarWorkflowParaSuperiorAtual(RegistrarWorkflowInternoCommand.aceiteValidacao(
                sp,
                novaSituacao,
                usuario,
                normalizarTexto(observacoes)
        ));

        log.info("Validação aceita para mapa do SP {}", sp.getCodigo());
    }

    public void homologarValidacao(Long codSubprocesso, @Nullable String observacoes) {
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validarLocalizacaoEscrita(sp, usuario);
        executarHomologacaoValidacao(sp, observacoes, usuario);
    }

    public void homologarValidacaoEmBloco(List<Long> subprocessoCodigos) {
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        List<Subprocesso> subprocessos = subprocessoRepo.buscarPorCodigosComMapaEAtividades(subprocessoCodigos);
        subprocessos.forEach(sp -> executarHomologacaoValidacao(sp, null, usuario));
    }

    private void executarHomologacaoValidacao(Subprocesso sp, @Nullable String observacoes, Usuario usuario) {
        log.info("Homologando validação do mapa do subprocesso {}", sp.getCodigo());
        validacaoService.validarSituacaoPermitida(sp,
                MAPEAMENTO_MAPA_VALIDADO,
                REVISAO_MAPA_VALIDADO);

        sp.setSituacao(obterSituacaoObrigatoria(SITUACAO_MAPA_HOMOLOGADO, sp, "homologação de validação"));

        registrarTransicaoDentroDoAdmin(sp, TipoTransicao.MAPA_HOMOLOGADO, usuario, normalizarTexto(observacoes));
    }

    private void registrarTransicaoParaSuperiorDaUnidade(
            Subprocesso sp,
            TipoTransicao tipoTransicao,
            Usuario usuario,
            @Nullable String observacoes
    ) {
        Unidade unidade = sp.getUnidade();
        Unidade unidadeSuperior = buscarSuperiorImediato(unidade.getCodigo());
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

    private void registrarWorkflowParaSuperiorAtual(RegistrarWorkflowInternoCommand cmd) {
        Unidade unidadeAtual = localizacaoSubprocessoService.obterLocalizacaoAtual(cmd.sp());
        Unidade unidadeDestino = buscarSuperiorImediato(unidadeAtual.getCodigo());

        RegistrarWorkflowInternoCommand.RegistrarWorkflowInternoCommandBuilder builder = RegistrarWorkflowInternoCommand.builder()
                .sp(cmd.sp())
                .novaSituacao(cmd.novaSituacao())
                .tipoTransicao(cmd.tipoTransicao())
                .tipoAnalise(cmd.tipoAnalise())
                .tipoAcaoAnalise(cmd.tipoAcaoAnalise())
                .unidadeAnalise(unidadeAtual)
                .usuario(cmd.usuario())
                .motivoAnalise(cmd.motivoAnalise())
                .observacoes(cmd.observacoes());

        if (unidadeDestino != null) {
            registrarWorkflowComDestino(builder.unidadeDestino(unidadeDestino).build());
        } else if (cmd.usuario().getPerfilAtivo() == Perfil.ADMIN) {
            // ADMIN na raiz: registra transição interna para si mesmo para mudar a situação
            registrarWorkflowComDestino(builder.unidadeDestino(unidadeAtual).build());
        }
    }

    private void validarLocalizacaoEscrita(Subprocesso sp, Usuario usuario) {
        Unidade localizacao = localizacaoSubprocessoService.obterLocalizacaoAtual(sp);
        if (!Objects.equals(usuario.getUnidadeAtivaCodigo(), localizacao.getCodigo())) {
            throw new ErroAcessoNegado("Operação não permitida: o subprocesso não está localizado na sua unidade ativa.");
        }
    }

    private void registrarWorkflowComDestino(RegistrarWorkflowInternoCommand cmd) {
        Unidade unidadeAnalise = Objects.requireNonNull(cmd.unidadeAnalise(), "Unidade de analise obrigatoria");
        Unidade unidadeDestino = Objects.requireNonNull(cmd.unidadeDestino(), "Unidade de destino obrigatoria");
        registrarAnalise(RegistrarWorkflowCommand.builder()
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
                .build());
    }

    private void registrarTransicaoDoAdminParaUnidade(
            Subprocesso sp,
            TipoTransicao tipoTransicao,
            Usuario usuario,
            @Nullable String observacoes
    ) {
        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(tipoTransicao)
                .origem(obterUnidadeAdmin())
                .destino(sp.getUnidade())
                .usuario(usuario)
                .observacoes(observacoes)
                .build());
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
                .build());
    }

    private Unidade obterUnidadeAdmin() {
        return unidadeService.buscarAdmin();
    }

    public void alterarDataLimite(Long codSubprocesso, LocalDate novaDataLimite) {
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        SituacaoSubprocesso situacaoSp = sp.getSituacao();
        LocalDate ultimaDataLimite = obterUltimaDataLimite(sp);

        if (ultimaDataLimite != null && novaDataLimite.isBefore(ultimaDataLimite)) {
            throw new sgc.comum.erros.ErroValidacao(Mensagens.DATA_LIMITE_MAIOR_OU_IGUAL_ULTIMA_DATA_SUBPROCESSO);
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
        String novaDataFormatada = novaDataLimite.format(DATE_FORMATTER);
        int etapa = obterEtapaPorSituacao(situacaoSp);
        notificarAlteracaoDataLimite(sp, novaDataFormatada, etapa);
    }

    private int obterEtapaPorSituacao(SituacaoSubprocesso situacaoSp) {
        return situacaoSp.ehEtapaMapa() ? 2 : 1;
    }

    private void notificarAlteracaoDataLimite(Subprocesso sp, String novaDataFormatada, int etapa) {
        notificacaoService.notificarAlteracaoDataLimite(sp, novaDataFormatada, etapa);
    }

    private @Nullable Unidade buscarSuperiorImediato(Long codigoUnidade) {
        Long codigoPai = unidadeHierarquiaService.buscarCodigoPai(codigoUnidade);
        if (codigoPai == null) {
            return null;
        }
        return unidadeService.buscarPorCodigo(codigoPai);
    }

    private Unidade obterUnidadeDevolucao(Subprocesso sp, Unidade unidadeAnalise) {
        List<Movimentacao> movimentacoes = movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(sp.getCodigo());

        return movimentacoes.stream()
                .filter(movimentacao -> Objects.equals(movimentacao.getUnidadeDestino().getCodigo(), unidadeAnalise.getCodigo()))
                .map(Movimentacao::getUnidadeOrigem)
                .filter(unidadeOrigem -> hierarquiaService.isSubordinada(unidadeOrigem, unidadeAnalise))
                .findFirst()
                .orElseThrow(() -> new ErroInconsistenciaInterna(
                        "Historico de movimentacoes inconsistente para devolucao do subprocesso %s na unidade %s"
                                .formatted(sp.getCodigo(), unidadeAnalise.getCodigo())
                ));
    }

    private SituacaoSubprocesso obterSituacaoObrigatoria(Map<TipoProcesso, SituacaoSubprocesso> situacoes, Subprocesso subprocesso, String contexto) {
        TipoProcesso tipoProcesso = subprocesso.getProcesso().getTipo();
        SituacaoSubprocesso situacao = situacoes.get(tipoProcesso);
        if (situacao == null) {
            throw new IllegalStateException("Tipo de processo %s sem situação configurada para %s".formatted(tipoProcesso, contexto));
        }
        return situacao;
    }

    @Builder
    private record RegistrarWorkflowInternoCommand(
            Subprocesso sp,
            SituacaoSubprocesso novaSituacao,
            TipoTransicao tipoTransicao,
            TipoAnalise tipoAnalise,
            TipoAcaoAnalise tipoAcaoAnalise,
            @Nullable Unidade unidadeAnalise,
            @Nullable Unidade unidadeDestino,
            Usuario usuario,
            @Nullable String motivoAnalise,
            @Nullable String observacoes
    ) {
        private static RegistrarWorkflowInternoCommand devolucaoValidacao(
                Subprocesso sp,
                SituacaoSubprocesso novaSituacao,
                Unidade unidadeAnalise,
                Unidade unidadeDevolucao,
                Usuario usuario,
                @Nullable String justificativa
        ) {
            return RegistrarWorkflowInternoCommand.builder()
                    .sp(sp)
                    .novaSituacao(novaSituacao)
                    .tipoTransicao(TipoTransicao.MAPA_VALIDACAO_DEVOLVIDA)
                    .tipoAnalise(TipoAnalise.VALIDACAO)
                    .tipoAcaoAnalise(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                    .unidadeAnalise(unidadeAnalise)
                    .unidadeDestino(unidadeDevolucao)
                    .usuario(usuario)
                    .motivoAnalise(justificativa)
                    .observacoes(justificativa)
                    .build();
        }

        private static RegistrarWorkflowInternoCommand aceiteValidacao(
                Subprocesso sp,
                SituacaoSubprocesso novaSituacao,
                Usuario usuario,
                @Nullable String observacoes
        ) {
            return RegistrarWorkflowInternoCommand.builder()
                    .sp(sp)
                    .novaSituacao(novaSituacao)
                    .tipoTransicao(TipoTransicao.MAPA_VALIDACAO_ACEITA)
                    .tipoAnalise(TipoAnalise.VALIDACAO)
                    .tipoAcaoAnalise(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                    .usuario(usuario)
                    .motivoAnalise("Aceite da validação")
                    .observacoes(observacoes)
                    .build();
        }
    }

}

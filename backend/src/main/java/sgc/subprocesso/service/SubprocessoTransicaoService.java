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
    private final SubprocessoConsultaService consultaService;
    private final SubprocessoValidacaoService validacaoService;
    private final SubprocessoNotificacaoService notificacaoService;
    private final UnidadeService unidadeService;
    private final HierarquiaService hierarquiaService;
    private final UsuarioFacade usuarioFacade;
    private final MapaManutencaoService mapaManutencaoService;
    private final EmailService emailService;
    private final AlertaFacade alertaService;

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

    private record FluxoCadastroContexto(
            String etapa,
            SituacaoSubprocesso situacaoDisponibilizada,
            SituacaoSubprocesso situacaoEmAndamento,
            SituacaoSubprocesso situacaoHomologada,
            TipoTransicao transicaoDevolucao,
            TipoTransicao transicaoAceite,
            TipoTransicao transicaoHomologacao,
            TipoAcaoAnalise acaoDevolucao,
            TipoAcaoAnalise acaoAceite
    ) {
        private static FluxoCadastroContexto revisao() {
            return new FluxoCadastroContexto(
                    ETAPA_REVISAO,
                    REVISAO_CADASTRO_DISPONIBILIZADA,
                    REVISAO_CADASTRO_EM_ANDAMENTO,
                    REVISAO_CADASTRO_HOMOLOGADA,
                    REVISAO_CADASTRO_DEVOLVIDA,
                    REVISAO_CADASTRO_ACEITA,
                    TipoTransicao.REVISAO_CADASTRO_HOMOLOGADA,
                    TipoAcaoAnalise.DEVOLUCAO_REVISAO,
                    TipoAcaoAnalise.ACEITE_REVISAO
            );
        }

        private static FluxoCadastroContexto mapeamento() {
            return new FluxoCadastroContexto(
                    ETAPA_CADASTRO,
                    MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                    MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                    MAPEAMENTO_CADASTRO_HOMOLOGADO,
                    CADASTRO_DEVOLVIDO,
                    CADASTRO_ACEITO,
                    TipoTransicao.CADASTRO_HOMOLOGADO,
                    TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO,
                    TipoAcaoAnalise.ACEITE_MAPEAMENTO
            );
        }
    }

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
            return new RegistrarWorkflowInternoCommand(
                    sp,
                    novaSituacao,
                    TipoTransicao.MAPA_VALIDACAO_DEVOLVIDA,
                    TipoAnalise.VALIDACAO,
                    TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO,
                    unidadeAnalise,
                    unidadeDevolucao,
                    usuario,
                    justificativa,
                    justificativa
            );
        }

        private static RegistrarWorkflowInternoCommand aceiteValidacao(
                Subprocesso sp,
                SituacaoSubprocesso novaSituacao,
                Usuario usuario,
                @Nullable String observacoes
        ) {
            return new RegistrarWorkflowInternoCommand(
                    sp,
                    novaSituacao,
                    TipoTransicao.MAPA_VALIDACAO_ACEITA,
                    TipoAnalise.VALIDACAO,
                    TipoAcaoAnalise.ACEITE_MAPEAMENTO,
                    null,
                    null,
                    usuario,
                    "Aceite da validação",
                    observacoes
            );
        }

        private static RegistrarWorkflowInternoCommand cadastroComDestino(
                Subprocesso sp,
                SituacaoSubprocesso novaSituacao,
                FluxoCadastroContexto contexto,
                Unidade unidadeAnalise,
                Unidade unidadeDestino,
                Usuario usuario,
                @Nullable String observacoes
        ) {
            return new RegistrarWorkflowInternoCommand(
                    sp,
                    novaSituacao,
                    contexto.transicaoDevolucao(),
                    TipoAnalise.CADASTRO,
                    contexto.acaoDevolucao(),
                    unidadeAnalise,
                    unidadeDestino,
                    usuario,
                    observacoes,
                    observacoes
            );
        }

        private static RegistrarWorkflowInternoCommand cadastroParaSuperiorAtual(
                Subprocesso sp,
                SituacaoSubprocesso novaSituacao,
                FluxoCadastroContexto contexto,
                Usuario usuario,
                @Nullable String observacoes
        ) {
            return new RegistrarWorkflowInternoCommand(
                    sp,
                    novaSituacao,
                    contexto.transicaoAceite(),
                    TipoAnalise.CADASTRO,
                    contexto.acaoAceite(),
                    null,
                    null,
                    usuario,
                    observacoes,
                    observacoes
            );
        }
    }

    private record ReaberturaCommand(
            Long codigo,
            String justificativa,
            SituacaoSubprocesso situacaoMinima,
            SituacaoSubprocesso novaSituacao,
            TipoTransicao tipoTransicao,
            boolean revisao
    ) {
        private static ReaberturaCommand cadastro(Long codigo, String justificativa) {
            return new ReaberturaCommand(
                    codigo,
                    justificativa,
                    MAPEAMENTO_MAPA_HOMOLOGADO,
                    MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                    TipoTransicao.CADASTRO_REABERTO,
                    false
            );
        }

        private static ReaberturaCommand revisao(Long codigo, String justificativa) {
            return new ReaberturaCommand(
                    codigo,
                    justificativa,
                    REVISAO_MAPA_HOMOLOGADO,
                    REVISAO_CADASTRO_EM_ANDAMENTO,
                    REVISAO_CADASTRO_REABERTA,
                    true
            );
        }
    }

    private record AlertaReaberturaContexto(
            Processo processo,
            Unidade unidadeOrigem,
            String justificativa,
            boolean revisao
    ) {
    }

    private static @Nullable String normalizarTexto(@Nullable String texto) {
        if (!StringUtils.hasText(texto)) {
            return null;
        }
        return texto.trim();
    }

    public void registrarTransicao(RegistrarTransicaoCommand cmd) {
        persistirTransicao(cmd);
        notificarTransicao(cmd);
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

    private void notificarTransicao(RegistrarTransicaoCommand cmd) {
        Subprocesso sp = cmd.sp();
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
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp, MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        disponibilizar(sp, MAPEAMENTO_CADASTRO_DISPONIBILIZADO, TipoTransicao.CADASTRO_DISPONIBILIZADO, usuario);
    }

    public void iniciarRevisaoCadastro(Long codSubprocesso) {
        log.info("Iniciando revisão do cadastro do subprocesso {}", codSubprocesso);
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp, NAO_INICIADO);

        sp.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
        subprocessoRepo.save(sp);
        log.info("Subprocesso {} transicionado para REVISAO_CADASTRO_EM_ANDAMENTO", codSubprocesso);
    }

    public void disponibilizarRevisao(Long codSubprocesso, Usuario usuario) {
        log.info("Disponibilizando revisão do subprocesso {}", codSubprocesso);
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp, REVISAO_CADASTRO_EM_ANDAMENTO);
        disponibilizar(sp, REVISAO_CADASTRO_DISPONIBILIZADA, TipoTransicao.REVISAO_CADASTRO_DISPONIBILIZADA, usuario);
    }

    private void disponibilizar(Subprocesso sp, SituacaoSubprocesso novaSituacao,
                                TipoTransicao transicao, Usuario usuario) {
        validacaoService.validarRequisitosNegocioParaDisponibilizacao(sp);

        sp.setSituacao(novaSituacao);
        sp.setDataFimEtapa1(LocalDateTime.now());
        registrarTransicaoParaSuperiorDaUnidade(sp, transicao, usuario, null);
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
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);

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

    public void submeterMapaAjustado(Long codSubprocesso, SubmeterMapaAjustadoRequest request, Usuario usuario) {
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
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
    public void apresentarSugestoes(Long codSubprocesso, @Nullable String sugestoes, Usuario usuario) {
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
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
    public void validarMapa(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp, MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO);

        sp.setSituacao(obterSituacaoObrigatoria(SITUACAO_MAPA_VALIDADO, sp, "validação de mapa"));
        sp.setDataFimEtapa2(LocalDateTime.now());

        registrarTransicaoParaSuperiorDaUnidade(sp, TipoTransicao.MAPA_VALIDADO, usuario, null);

        log.info("Validado mapa do SP {}", codSubprocesso);
    }

    public void devolverValidacao(Long codSubprocesso, @Nullable String justificativa, Usuario usuario) {
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp,
                MAPEAMENTO_MAPA_COM_SUGESTOES,
                MAPEAMENTO_MAPA_VALIDADO,
                REVISAO_MAPA_COM_SUGESTOES,
                REVISAO_MAPA_VALIDADO);

        Unidade unidadeAnalise = consultaService.obterUnidadeLocalizacao(sp);
        Unidade unidadeDevolucao = obterUnidadeDevolucao(sp, unidadeAnalise);

        SituacaoSubprocesso novaSituacao = obterSituacaoObrigatoria(SITUACAO_MAPA_DISPONIBILIZADO, sp, "devolução de validação");
        sp.setDataFimEtapa2(null);

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

    public void aceitarValidacao(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        executarAceiteValidacao(codSubprocesso, observacoes, usuario);
    }

    public void aceitarValidacaoEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarAceiteValidacao(codSubprocesso, null, usuario));
    }

    private void executarAceiteValidacao(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
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
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp,
                MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO,
                REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO);

        sp.setSituacao(obterSituacaoObrigatoria(SITUACAO_MAPA_HOMOLOGADO, sp, "homologação de validação"));

        registrarTransicaoDentroDoAdmin(sp, TipoTransicao.MAPA_HOMOLOGADO, usuario, normalizarTexto(observacoes));
    }

    public void devolverCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarDevolucao(codSubprocesso, usuario, observacoes, false);
    }

    public void devolverRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarDevolucao(codSubprocesso, usuario, observacoes, true);
    }

    private void executarDevolucao(Long codSubprocesso, Usuario usuario, @Nullable String observacoes, boolean isRevisao) {
        FluxoCadastroContexto contexto = obterContextoCadastro(isRevisao);
        log.info("Devolvendo {} do subprocesso {}", contexto.etapa(), codSubprocesso);
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp, contexto.situacaoDisponibilizada());

        Unidade unidadeAnalise = consultaService.obterUnidadeLocalizacao(sp);
        Unidade unidadeDevolucao = obterUnidadeDevolucao(sp, unidadeAnalise);

        SituacaoSubprocesso novaSituacao = contexto.situacaoDisponibilizada();
        if (Objects.equals(unidadeDevolucao.getCodigo(), sp.getUnidade().getCodigo())) {
            novaSituacao = contexto.situacaoEmAndamento();
            sp.setDataFimEtapa1(null);
        }

        registrarWorkflowComDestino(RegistrarWorkflowInternoCommand.cadastroComDestino(
                sp,
                novaSituacao,
                contexto,
                unidadeAnalise,
                unidadeDevolucao,
                usuario,
                normalizarTexto(observacoes)
        ));
    }

    private Unidade obterUnidadeDevolucao(Subprocesso sp, Unidade unidadeAnalise) {
        List<Movimentacao> movimentacoes = movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(sp.getCodigo());

        return movimentacoes.stream()
                .filter(movimentacao -> Objects.equals(movimentacao.getUnidadeDestino().getCodigo(), unidadeAnalise.getCodigo()))
                .map(Movimentacao::getUnidadeOrigem)
                .filter(unidadeOrigem -> hierarquiaService.isSubordinada(unidadeOrigem, unidadeAnalise))
                .findFirst()
                .orElse(sp.getUnidade());
    }

    private void registrarTransicaoParaSuperiorDaUnidade(
            Subprocesso sp,
            TipoTransicao tipoTransicao,
            Usuario usuario,
            @Nullable String observacoes
    ) {
        Unidade unidade = sp.getUnidade();
        if (unidade.getUnidadeSuperior() != null) {
            registrarTransicao(RegistrarTransicaoCommand.builder()
                    .sp(sp)
                    .tipo(tipoTransicao)
                    .origem(unidade)
                    .destino(unidade.getUnidadeSuperior())
                    .usuario(usuario)
                    .observacoes(observacoes)
                    .build());
        }
    }

    private void registrarWorkflowParaSuperiorAtual(RegistrarWorkflowInternoCommand cmd) {
        Unidade unidadeAtual = consultaService.obterUnidadeLocalizacao(cmd.sp());
        Unidade unidadeDestino = unidadeAtual.getUnidadeSuperior();

        if (unidadeDestino != null) {
            registrarWorkflowComDestino(new RegistrarWorkflowInternoCommand(
                    cmd.sp(),
                    cmd.novaSituacao(),
                    cmd.tipoTransicao(),
                    cmd.tipoAnalise(),
                    cmd.tipoAcaoAnalise(),
                    unidadeAtual,
                    unidadeDestino,
                    cmd.usuario(),
                    cmd.motivoAnalise(),
                    cmd.observacoes()
            ));
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

    public void aceitarCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarAceite(codSubprocesso, usuario, observacoes, false);
    }

    public void aceitarRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarAceite(codSubprocesso, usuario, observacoes, true);
    }

    @Transactional
    public void aceitarCadastroEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> {
            boolean isRevisao = isRevisao(codSubprocesso);
            executarAceite(codSubprocesso, usuario, "Avaliação em bloco", isRevisao);
        });
    }

    private void executarAceite(Long codSubprocesso, Usuario usuario, @Nullable String observacoes, boolean isRevisao) {
        FluxoCadastroContexto contexto = obterContextoCadastro(isRevisao);
        log.info("Aceitando {} do subprocesso {}", contexto.etapa(), codSubprocesso);
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp, contexto.situacaoDisponibilizada());

        registrarWorkflowParaSuperiorAtual(RegistrarWorkflowInternoCommand.cadastroParaSuperiorAtual(
                sp,
                contexto.situacaoDisponibilizada(),
                contexto,
                usuario,
                normalizarTexto(observacoes)
        ));
    }

    public void homologarCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarHomologacao(codSubprocesso, usuario, observacoes, false);
    }

    public void homologarRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarHomologacao(codSubprocesso, usuario, observacoes, true);
    }

    public void homologarCadastroEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> {
            boolean isRevisao = isRevisao(codSubprocesso);
            executarHomologacao(codSubprocesso, usuario, "Homologação em bloco", isRevisao);
        });
    }

    private void executarHomologacao(Long codSubprocesso, Usuario usuario, @Nullable String observacoes, boolean isRevisao) {
        FluxoCadastroContexto contexto = obterContextoCadastro(isRevisao);
        log.info("Homologando {} do subprocesso {}", contexto.etapa(), codSubprocesso);
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp, contexto.situacaoDisponibilizada());

        sp.setSituacao(contexto.situacaoHomologada());

        registrarTransicaoDentroDoAdmin(sp, contexto.transicaoHomologacao(), usuario, normalizarTexto(observacoes));

        executarEfeitosDerivadosHomologacaoCadastro(sp, isRevisao);
    }

    private void executarEfeitosDerivadosHomologacaoCadastro(Subprocesso sp, boolean isRevisao) {
        String descAlerta = isRevisao
                ? Mensagens.ALERTA_REVISAO_HOMOLOGADA.formatted(sp.getUnidade().getSigla())
                : Mensagens.ALERTA_CADASTRO_HOMOLOGADO.formatted(sp.getUnidade().getSigla());
        Unidade admin = obterUnidadeAdmin();
        alertaService.criarAlertaTransicao(sp.getProcesso(), descAlerta, admin, sp.getUnidade());
    }

    public void reabrirCadastro(Long codigo, String justificativa) {
        executarReabertura(ReaberturaCommand.cadastro(codigo, justificativa));
    }

    public void reabrirRevisaoCadastro(Long codigo, String justificativa) {
        executarReabertura(ReaberturaCommand.revisao(codigo, justificativa));
    }

    private void executarReabertura(ReaberturaCommand cmd) {

        Subprocesso sp = consultaService.buscarSubprocesso(cmd.codigo());
        validacaoService.validarSituacaoMinima(sp,
                cmd.situacaoMinima(),
                Mensagens.ERRO_SUBPROCESSO_EM_FASE.formatted(cmd.revisao() ? ETAPA_REVISAO : ETAPA_CADASTRO)
        );

        Usuario usuario = usuarioFacade.usuarioAutenticado();

        sp.setSituacao(cmd.novaSituacao());
        sp.setDataFimEtapa1(null);

        registrarTransicaoDoAdminParaUnidade(sp, cmd.tipoTransicao(), usuario, cmd.justificativa());

        enviarAlertasReabertura(new AlertaReaberturaContexto(sp.getProcesso(), sp.getUnidade(), cmd.justificativa(), cmd.revisao()));
        log.info("Reaberto {} do SP {}", cmd.revisao() ? ETAPA_REVISAO : ETAPA_CADASTRO, cmd.codigo());
    }

    private void enviarAlertasReabertura(AlertaReaberturaContexto contexto) {
        Unidade unidade = contexto.unidadeOrigem();

        criarAlertaReaberturaUnidade(contexto);

        Unidade superior = unidade.getUnidadeSuperior();
        while (superior != null) {
            criarAlertaReaberturaSuperior(contexto, superior);
            superior = superior.getUnidadeSuperior();
        }
    }

    private void criarAlertaReaberturaUnidade(AlertaReaberturaContexto contexto) {
        if (contexto.revisao()) {
            alertaService.criarAlertaReaberturaRevisao(contexto.processo(), contexto.unidadeOrigem(), contexto.justificativa());
            return;
        }
        alertaService.criarAlertaReaberturaCadastro(contexto.processo(), contexto.unidadeOrigem());
    }

    private void criarAlertaReaberturaSuperior(AlertaReaberturaContexto contexto, Unidade unidadeSuperior) {
        if (contexto.revisao()) {
            alertaService.criarAlertaReaberturaRevisaoSuperior(contexto.processo(), unidadeSuperior, contexto.unidadeOrigem());
            return;
        }
        alertaService.criarAlertaReaberturaCadastroSuperior(contexto.processo(), unidadeSuperior, contexto.unidadeOrigem());
    }

    public void alterarDataLimite(Long codSubprocesso, LocalDate novaDataLimite) {
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        String situacaoSp = sp.getSituacao().name();
        LocalDate ultimaDataLimite = obterUltimaDataLimite(sp);

        if (ultimaDataLimite != null && novaDataLimite.isBefore(ultimaDataLimite)) {
            throw new sgc.comum.erros.ErroValidacao(Mensagens.DATA_LIMITE_MAIOR_OU_IGUAL_ULTIMA_DATA_SUBPROCESSO);
        }

        atualizarDataLimitePorSituacao(sp, situacaoSp, novaDataLimite.atStartOfDay());

        subprocessoRepo.save(sp);

        executarEfeitosDerivadosAlteracaoDataLimite(sp, novaDataLimite, situacaoSp);
    }

    private void atualizarDataLimitePorSituacao(Subprocesso sp, String situacaoSp, LocalDateTime novaDataLimiteInicioDoDia) {
        if (situacaoSp.contains("MAPA")) {
            sp.setDataLimiteEtapa2(novaDataLimiteInicioDoDia);
            return;
        }
        sp.setDataLimiteEtapa1(novaDataLimiteInicioDoDia);
    }

    private void executarEfeitosDerivadosAlteracaoDataLimite(Subprocesso sp, LocalDate novaDataLimite, String situacaoSp) {
        String novaDataStr = novaDataLimite.format(DATE_FORMATTER);
        enviarEmailAlteracaoDataLimite(sp, novaDataStr);
        criarAlertaAlteracaoDataLimite(sp, situacaoSp, novaDataStr);
    }

    private void enviarEmailAlteracaoDataLimite(Subprocesso sp, String novaDataStr) {
        String assunto = Mensagens.ASSUNTO_DATA_LIMITE_ALTERADA;
        String corpo = Mensagens.CORPO_DATA_LIMITE_ALTERADA
                .formatted(sp.getUnidade().getSigla(), sp.getProcesso().getDescricao(), novaDataStr);

        String emailDestino = notificacaoService.getEmailUnidade(sp.getUnidade());
        emailService.enviarEmail(emailDestino, assunto, corpo);
    }

    private void criarAlertaAlteracaoDataLimite(Subprocesso sp, String situacaoSp, String novaDataStr) {
        int etapa = obterEtapaPorSituacao(situacaoSp);
        alertaService.criarAlertaAlteracaoDataLimite(sp.getProcesso(), sp.getUnidade(), novaDataStr, etapa);
    }

    private int obterEtapaPorSituacao(String situacaoSp) {
        return situacaoSp.contains("MAPA") ? 2 : 1;
    }


    private SituacaoSubprocesso obterSituacaoObrigatoria(Map<TipoProcesso, SituacaoSubprocesso> situacoes, Subprocesso subprocesso, String contexto) {
        TipoProcesso tipoProcesso = subprocesso.getProcesso().getTipo();
        SituacaoSubprocesso situacao = situacoes.get(tipoProcesso);
        if (situacao == null) {
            throw new IllegalStateException("Tipo de processo %s sem situação configurada para %s".formatted(tipoProcesso, contexto));
        }
        return situacao;
    }

    private boolean isRevisao(Long codSubprocesso) {
        return REVISAO == consultaService.buscarSubprocesso(codSubprocesso).getProcesso().getTipo();
    }

    private FluxoCadastroContexto obterContextoCadastro(boolean isRevisao) {
        if (isRevisao) {
            return FluxoCadastroContexto.revisao();
        }
        return FluxoCadastroContexto.mapeamento();
    }

}

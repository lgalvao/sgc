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

        registrarTransicaoDoAdminParaUnidade(sp, TipoTransicao.MAPA_DISPONIBILIZADO, usuario, observacoes);
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

        registrarTransicaoParaSuperiorDaUnidade(sp, TipoTransicao.MAPA_SUGESTOES_APRESENTADAS, usuario, sugestoes);

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

        registrarWorkflowComDestino(
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
        registrarWorkflowParaSuperiorAtual(
                sp,
                novaSituacao,
                TipoTransicao.MAPA_VALIDACAO_ACEITA,
                TipoAnalise.VALIDACAO,
                TipoAcaoAnalise.ACEITE_MAPEAMENTO,
                usuario,
                "Aceite da validação",
                observacoes
        );

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

        registrarTransicaoDentroDoAdmin(sp, TipoTransicao.MAPA_HOMOLOGADO, usuario, observacoes);
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

        registrarWorkflowComDestino(
                sp,
                novaSituacao,
                contexto.transicaoDevolucao(),
                TipoAnalise.CADASTRO,
                contexto.acaoDevolucao(),
                unidadeAnalise,
                unidadeDevolucao,
                usuario,
                observacoes,
                observacoes
        );
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
        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(tipoTransicao)
                .origem(unidade)
                .destino(unidade.getUnidadeSuperior())
                .usuario(usuario)
                .observacoes(observacoes)
                .build());
    }

    private void registrarWorkflowParaSuperiorAtual(
            Subprocesso sp,
            SituacaoSubprocesso novaSituacao,
            TipoTransicao tipoTransicao,
            TipoAnalise tipoAnalise,
            TipoAcaoAnalise tipoAcaoAnalise,
            Usuario usuario,
            @Nullable String motivoAnalise,
            @Nullable String observacoes
    ) {
        Unidade unidadeAtual = consultaService.obterUnidadeLocalizacao(sp);
        Unidade unidadeDestino = unidadeAtual.getUnidadeSuperior();

        registrarWorkflowComDestino(
                sp,
                novaSituacao,
                tipoTransicao,
                tipoAnalise,
                tipoAcaoAnalise,
                unidadeAtual,
                unidadeDestino,
                usuario,
                motivoAnalise,
                observacoes
        );
    }

    private void registrarWorkflowComDestino(
            Subprocesso sp,
            SituacaoSubprocesso novaSituacao,
            TipoTransicao tipoTransicao,
            TipoAnalise tipoAnalise,
            TipoAcaoAnalise tipoAcaoAnalise,
            Unidade unidadeAnalise,
            Unidade unidadeDestino,
            Usuario usuario,
            @Nullable String motivoAnalise,
            @Nullable String observacoes
    ) {
        registrarAnalise(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(novaSituacao)
                .tipoTransicao(tipoTransicao)
                .tipoAnalise(tipoAnalise)
                .tipoAcaoAnalise(tipoAcaoAnalise)
                .unidadeAnalise(unidadeAnalise)
                .unidadeOrigemTransicao(unidadeAnalise)
                .unidadeDestinoTransicao(unidadeDestino)
                .usuario(usuario)
                .motivoAnalise(motivoAnalise)
                .observacoes(observacoes)
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
        return unidadeService.buscarPorSigla(SIGLA_ADMIN);
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

        registrarWorkflowParaSuperiorAtual(
                sp,
                contexto.situacaoDisponibilizada(),
                contexto.transicaoAceite(),
                TipoAnalise.CADASTRO,
                contexto.acaoAceite(),
                usuario,
                observacoes,
                observacoes
        );
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

        registrarTransicaoDentroDoAdmin(sp, contexto.transicaoHomologacao(), usuario, observacoes);

        String descAlerta = isRevisao
                ? Mensagens.ALERTA_REVISAO_HOMOLOGADA.formatted(sp.getUnidade().getSigla())
                : Mensagens.ALERTA_CADASTRO_HOMOLOGADO.formatted(sp.getUnidade().getSigla());
        Unidade admin = obterUnidadeAdmin();
        alertaService.criarAlertaTransicao(sp.getProcesso(), descAlerta, admin, sp.getUnidade());
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

        Subprocesso sp = consultaService.buscarSubprocesso(codigo);
        validacaoService.validarSituacaoMinima(sp,
                situacaoMinima,
                Mensagens.ERRO_SUBPROCESSO_EM_FASE.formatted(isRevisao ? ETAPA_REVISAO : ETAPA_CADASTRO)
        );

        Usuario usuario = usuarioFacade.usuarioAutenticado();

        sp.setSituacao(novaSituacao);
        sp.setDataFimEtapa1(null);

        registrarTransicaoDoAdminParaUnidade(sp, tipoTransicao, usuario, justificativa);

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
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
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

package sgc.subprocesso.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.*;
import sgc.comum.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA;
import static sgc.subprocesso.model.TipoTransicao.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CadastroFluxoService {

    private static final String ETAPA_REVISAO = "revisão";
    private static final String ETAPA_CADASTRO = "cadastro";
    private final SubprocessoRepo subprocessoRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final SubprocessoConsultaService consultaService;
    private final LocalizacaoSubprocessoService localizacaoSubprocessoService;
    private final SubprocessoValidacaoService validacaoService;
    private final UsuarioAplicacaoService usuarioAplicacaoService;
    private final UnidadeService unidadeService;
    private final HierarquiaService hierarquiaService;
    private final UnidadeHierarquiaService unidadeHierarquiaService;
    private final AlertaAplicacaoService alertaService;
    private final SubprocessoTransicaoService transicaoService;
    private final SubprocessoNotificacaoService notificacaoService;

    private static @Nullable String normalizarTexto(@Nullable String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    @Transactional
    public void disponibilizarCadastro(Long codSubprocesso) {
        disponibilizarCadastro(codSubprocesso, null);
    }

    @Transactional
    public void disponibilizarCadastro(Long codSubprocesso, @Nullable String observacoes) {
        log.info("Disponibilizando cadastro do subprocesso {}", codSubprocesso);
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp, MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        disponibilizar(sp, MAPEAMENTO_CADASTRO_DISPONIBILIZADO, TipoTransicao.CADASTRO_DISPONIBILIZADO, usuario, observacoes);
    }

    public void iniciarRevisaoCadastro(Long codSubprocesso) {
        log.info("Iniciando revisão do cadastro do subprocesso {}", codSubprocesso);
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        if (sp.getProcesso() != null && sp.getProcesso().getSituacao() == SituacaoProcesso.FINALIZADO) {
            throw new sgc.comum.erros.ErroValidacao("Não é permitido iniciar revisão de cadastro em processo finalizado.");
        }
        validacaoService.validarSituacaoPermitida(sp, NAO_INICIADO);

        sp.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
        subprocessoRepo.save(sp);
        log.info("Subprocesso {} transicionado para REVISAO_CADASTRO_EM_ANDAMENTO", codSubprocesso);
    }

    public void cancelarInicioRevisaoCadastro(Long codSubprocesso) {
        log.info("Cancelando início da revisão do cadastro do subprocesso {}", codSubprocesso);
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp, REVISAO_CADASTRO_EM_ANDAMENTO);

        sp.setSituacao(NAO_INICIADO);
        subprocessoRepo.save(sp);
        log.info("Subprocesso {} transicionado para NAO_INICIADO", codSubprocesso);
    }

    public void disponibilizarRevisao(Long codSubprocesso) {
        disponibilizarRevisao(codSubprocesso, null);
    }

    public void disponibilizarRevisao(Long codSubprocesso, @Nullable String observacoes) {
        log.info("Disponibilizando revisão do subprocesso {}", codSubprocesso);
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp, REVISAO_CADASTRO_EM_ANDAMENTO);
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        disponibilizar(sp, REVISAO_CADASTRO_DISPONIBILIZADA, TipoTransicao.REVISAO_CADASTRO_DISPONIBILIZADA, usuario, observacoes);
    }

    public void devolver(Long codSubprocesso, @Nullable String observacoes) {
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        executarDevolucao(sp, usuario, observacoes);
    }

    public void aceitar(Long codSubprocesso, @Nullable String observacoes) {
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        executarAceite(sp, usuario, observacoes);
    }

    @Transactional
    public void aceitarCadastroEmBloco(List<Long> subprocessoCodigos) {
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        List<Subprocesso> subprocessos = subprocessoRepo.buscarPorCodigosComMapaEAtividades(subprocessoCodigos);
        subprocessos.forEach(sp -> executarAceite(sp, usuario, "Avaliação em bloco", false));
        notificacaoService.notificarAceiteCadastroEmBloco(subprocessos);
    }

    public void homologar(Long codSubprocesso, @Nullable String observacoes) {
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        executarHomologacao(sp, usuario, observacoes);
    }

    public void homologarCadastroEmBloco(List<Long> subprocessoCodigos) {
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        List<Subprocesso> subprocessos = subprocessoRepo.buscarPorCodigosComMapaEAtividades(subprocessoCodigos);
        subprocessos.forEach(sp -> executarHomologacao(sp, usuario, "Homologação em bloco"));
    }

    public void reabrirCadastro(Long codigo, String justificativa) {
        executarReabertura(ReaberturaCommand.cadastro(codigo, justificativa));
    }

    public void reabrirRevisaoCadastro(Long codigo, String justificativa) {
        executarReabertura(ReaberturaCommand.revisao(codigo, justificativa));
    }

    private void disponibilizar(Subprocesso sp, SituacaoSubprocesso novaSituacao,
                                TipoTransicao transicao, Usuario usuario, @Nullable String observacoes) {
        validacaoService.validarRequisitosNegocioParaDisponibilizacao(sp);
        sp.setSituacao(novaSituacao);
        sp.setDataFimEtapa1(LocalDateTime.now());

        Unidade unidade = sp.getUnidade();
        Unidade unidadeSuperior = buscarSuperiorImediato(unidade.getCodigo());
        if (unidadeSuperior != null) {
            transicaoService.registrarTransicao(RegistrarTransicaoCommand.builder()
                    .sp(sp)
                    .tipo(transicao)
                    .origem(unidade)
                    .destino(unidadeSuperior)
                    .usuario(usuario)
                    .observacoes(normalizarTexto(observacoes))
                    .build());
        }
    }

    private void executarDevolucao(Subprocesso sp, Usuario usuario, @Nullable String observacoes) {
        FluxoCadastroContexto contexto = obterContextoCadastro(sp);
        validacaoService.validarSituacaoPermitida(sp, contexto.situacaoDisponibilizada());

        Unidade unidadeAnalise = localizacaoSubprocessoService.obterLocalizacaoAtual(sp);
        Unidade unidadeDevolucao = obterUnidadeDevolucao(sp, unidadeAnalise);

        SituacaoSubprocesso novaSituacao = contexto.situacaoDisponibilizada();
        if (Objects.equals(unidadeDevolucao.getCodigo(), sp.getUnidade().getCodigo())) {
            novaSituacao = contexto.situacaoEmAndamento();
            sp.setDataFimEtapa1(null);
        }

        String obs = normalizarTexto(observacoes);
        transicaoService.registrarAnalise(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(novaSituacao)
                .tipoTransicao(contexto.transicaoDevolucao())
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(contexto.acaoDevolucao())
                .unidadeAnalise(unidadeAnalise)
                .unidadeOrigemTransicao(unidadeAnalise)
                .unidadeDestinoTransicao(unidadeDevolucao)
                .usuario(usuario)
                .motivoAnalise(null)
                .observacoes(obs)
                .build());

        log.info("Devolvido {} do subprocesso {}", contexto.etapa(), sp.getCodigo());
    }

    private void executarAceite(Subprocesso sp, Usuario usuario, @Nullable String observacoes) {
        executarAceite(sp, usuario, observacoes, true);
    }

    private void executarAceite(Subprocesso sp, Usuario usuario, @Nullable String observacoes, boolean enviarEmails) {
        FluxoCadastroContexto contexto = obterContextoCadastro(sp);
        log.info("Aceitando {} do subprocesso {}", contexto.etapa(), sp.getCodigo());
        validacaoService.validarSituacaoPermitida(sp, contexto.situacaoDisponibilizada());

        Unidade unidadeAtual = localizacaoSubprocessoService.obterLocalizacaoAtual(sp);
        Unidade unidadeDestino = buscarSuperiorImediato(unidadeAtual.getCodigo());
        if (unidadeDestino != null) {
            String obs = normalizarTexto(observacoes);
            RegistrarWorkflowCommand cmd = RegistrarWorkflowCommand.builder()
                    .sp(sp)
                    .novaSituacao(contexto.situacaoDisponibilizada())
                    .tipoTransicao(contexto.transicaoAceite())
                    .tipoAnalise(TipoAnalise.CADASTRO)
                    .tipoAcaoAnalise(contexto.acaoAceite())
                    .unidadeAnalise(unidadeAtual)
                    .unidadeOrigemTransicao(unidadeAtual)
                    .unidadeDestinoTransicao(unidadeDestino)
                    .usuario(usuario)
                    .motivoAnalise(null)
                    .observacoes(obs)
                    .notificarSuperior(enviarEmails ? null : Boolean.FALSE)
                    .build();
            if (enviarEmails) {
                transicaoService.registrarAnalise(cmd);
            } else {
                transicaoService.registrarAnaliseSemEmail(cmd);
            }
        }
    }

    private void executarHomologacao(Subprocesso sp, Usuario usuario, @Nullable String observacoes) {
        FluxoCadastroContexto contexto = obterContextoCadastro(sp);
        log.info("Homologando {} do subprocesso {}", contexto.etapa(), sp.getCodigo());
        validacaoService.validarSituacaoPermitida(sp, contexto.situacaoDisponibilizada());

        sp.setSituacao(contexto.situacaoHomologada());

        Unidade admin = unidadeService.buscarAdmin();
        transicaoService.registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(contexto.transicaoHomologacao())
                .origem(admin)
                .destino(admin)
                .usuario(usuario)
                .observacoes(normalizarTexto(observacoes))
                .build());
    }

    private void executarReabertura(ReaberturaCommand cmd) {
        Subprocesso sp = consultaService.buscarSubprocesso(cmd.codigo());
        validacaoService.validarSituacaoMinima(sp,
                cmd.situacaoMinima(),
                Mensagens.ERRO_SUBPROCESSO_EM_FASE.formatted(cmd.revisao() ? ETAPA_REVISAO : ETAPA_CADASTRO)
        );

        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();

        sp.setSituacao(cmd.novaSituacao());
        sp.setDataFimEtapa1(null);

        Unidade admin = unidadeService.buscarAdmin();
        transicaoService.registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(cmd.tipoTransicao())
                .origem(admin)
                .destino(sp.getUnidade())
                .usuario(usuario)
                .observacoes(cmd.justificativa())
                .build());

        criarAlertasReabertura(new AlertaReaberturaContexto(sp.getProcesso(), sp.getUnidade(), cmd.justificativa(), cmd.revisao()));
        log.info("Reaberto {} do SP {}", cmd.revisao() ? ETAPA_REVISAO : ETAPA_CADASTRO, cmd.codigo());
    }

    private void criarAlertasReabertura(AlertaReaberturaContexto contexto) {
        Unidade unidade = contexto.unidadeOrigem();
        criarAlertaReaberturaUnidade(contexto);

        Unidade superiorImediato = buscarSuperiorImediato(unidade.getCodigo());
        if (superiorImediato != null) {
            criarAlertaReaberturaSuperior(contexto, superiorImediato);
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
                .orElseThrow(() -> new sgc.comum.erros.ErroInconsistenciaInterna(
                        "Historico de movimentacoes inconsistente para devolucao do subprocesso %s na unidade %s"
                                .formatted(sp.getCodigo(), unidadeAnalise.getCodigo())
                ));
    }

    private FluxoCadastroContexto obterContextoCadastro(Subprocesso sp) {
        return switch (sp.getProcesso().getTipo()) {
            case REVISAO -> FluxoCadastroContexto.revisao();
            case MAPEAMENTO -> FluxoCadastroContexto.mapeamento();
            default -> throw new sgc.comum.erros.ErroInconsistenciaInterna(
                    "Tipo %s sem fluxo de cadastro definido".formatted(sp.getProcesso().getTipo()));
        };
    }

    @Builder
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
            return FluxoCadastroContexto.builder()
                    .etapa(ETAPA_REVISAO)
                    .situacaoDisponibilizada(REVISAO_CADASTRO_DISPONIBILIZADA)
                    .situacaoEmAndamento(REVISAO_CADASTRO_EM_ANDAMENTO)
                    .situacaoHomologada(REVISAO_CADASTRO_HOMOLOGADA)
                    .transicaoDevolucao(REVISAO_CADASTRO_DEVOLVIDA)
                    .transicaoAceite(REVISAO_CADASTRO_ACEITA)
                    .transicaoHomologacao(TipoTransicao.REVISAO_CADASTRO_HOMOLOGADA)
                    .acaoDevolucao(TipoAcaoAnalise.DEVOLUCAO_REVISAO)
                    .acaoAceite(TipoAcaoAnalise.ACEITE_REVISAO)
                    .build();
        }

        private static FluxoCadastroContexto mapeamento() {
            return FluxoCadastroContexto.builder()
                    .etapa(ETAPA_CADASTRO)
                    .situacaoDisponibilizada(MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                    .situacaoEmAndamento(MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                    .situacaoHomologada(MAPEAMENTO_CADASTRO_HOMOLOGADO)
                    .transicaoDevolucao(CADASTRO_DEVOLVIDO)
                    .transicaoAceite(CADASTRO_ACEITO)
                    .transicaoHomologacao(TipoTransicao.CADASTRO_HOMOLOGADO)
                    .acaoDevolucao(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                    .acaoAceite(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                    .build();
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
}

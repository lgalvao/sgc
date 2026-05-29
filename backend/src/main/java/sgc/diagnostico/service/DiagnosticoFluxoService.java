package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.diagnostico.model.Diagnostico;
import sgc.diagnostico.model.DiagnosticoRepo;
import sgc.diagnostico.model.SituacaoDiagnostico;
import org.jspecify.annotations.Nullable;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.HierarquiaService;
import sgc.organizacao.service.UnidadeHierarquiaService;
import sgc.organizacao.service.UnidadeService;
import sgc.organizacao.service.UsuarioService;
import sgc.organizacao.UsuarioFacade;
import sgc.subprocesso.dto.RegistrarTransicaoCommand;
import sgc.subprocesso.dto.RegistrarWorkflowCommand;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoAcaoAnalise;
import sgc.subprocesso.model.TipoAnalise;
import sgc.subprocesso.model.TipoTransicao;
import sgc.subprocesso.service.SubprocessoConsultaService;
import sgc.subprocesso.service.LocalizacaoSubprocessoService;
import sgc.subprocesso.service.SubprocessoTransicaoService;
import sgc.subprocesso.service.SubprocessoValidacaoService;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.Competencia;
import sgc.diagnostico.model.AvaliacaoServidor;
import sgc.diagnostico.model.SituacaoAvaliacaoServidor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class DiagnosticoFluxoService {
    private final DiagnosticoRepo diagnosticoRepo;
    private final DiagnosticoValidacaoService validacaoService;
    private final DiagnosticoNotificacaoService notificacaoService;
    private final SubprocessoConsultaService subprocessoConsultaService;
    private final SubprocessoTransicaoService transicaoService;
    private final SubprocessoValidacaoService subprocessoValidacaoService;
    private final LocalizacaoSubprocessoService localizacaoSubprocessoService;
    private final UnidadeService unidadeService;
    private final UnidadeHierarquiaService unidadeHierarquiaService;
    private final HierarquiaService hierarquiaService;
    private final UsuarioFacade usuarioFacade;
    private final UsuarioService usuarioService;

    public void inicializarDiagnostico(Subprocesso subprocesso) {
        Diagnostico diagnostico = Diagnostico.builder()
                .subprocesso(subprocesso)
                .situacao(SituacaoDiagnostico.EM_ANDAMENTO)
                .build();

        List<Usuario> servidores = usuarioService.buscarPorUnidadeLotacao(subprocesso.getUnidade().getCodigo());
        List<AvaliacaoServidor> avaliacoes = new ArrayList<>();

        Mapa mapa = subprocesso.getMapa();
        if (mapa != null && mapa.getCompetencias() != null) {
            for (Usuario servidor : servidores) {
                for (Competencia competencia : mapa.getCompetencias()) {
                    AvaliacaoServidor avaliacao = AvaliacaoServidor.builder()
                            .diagnostico(diagnostico)
                            .servidor(servidor)
                            .competencia(competencia)
                            .situacaoServidor(SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_REALIZADA)
                            .build();
                    avaliacoes.add(avaliacao);
                }
            }
        }

        diagnostico.setAvaliacaoServidores(avaliacoes);
        diagnosticoRepo.save(diagnostico);
    }

    public void concluirDiagnosticoUnidade(Long codSubprocesso) {
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));
        validacaoService.validarConclusaoUnidade(diagnostico.getCodigo());

        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);

        diagnostico.setSituacao(SituacaoDiagnostico.CONCLUIDO);
        diagnostico.setDataConclusao(LocalDateTime.now());
        diagnostico.setJustificativaConclusao(null);

        subprocesso.setSituacao(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        subprocesso.setDataFimEtapa1(LocalDateTime.now());

        Usuario usuario = usuarioFacade.usuarioAutenticado();
        Unidade unidadeOrigem = subprocesso.getUnidade();
        Unidade unidadeDestino = buscarSuperiorImediato(unidadeOrigem.getCodigo());
        if (unidadeDestino == null) {
            unidadeDestino = unidadeOrigem;
        }

        transicaoService.registrarTransicaoSemEmail(RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(TipoTransicao.DIAGNOSTICO_CONCLUIDO)
                .origem(unidadeOrigem)
                .destino(unidadeDestino)
                .usuario(usuario)
                .observacoes(null)
                .build());

        if (!Objects.equals(unidadeDestino.getCodigo(), unidadeOrigem.getCodigo())) {
            notificacaoService.notificarDiagnosticoConcluido(subprocesso, unidadeDestino);
        }
    }

    public void devolverDiagnostico(Long codSubprocesso, @Nullable String observacao) {
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));
        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        Unidade unidadeAnalise = localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso);
        Unidade unidadeDevolucao = obterUnidadeDevolucao(subprocesso, unidadeAnalise);

        SituacaoSubprocesso novaSituacao = SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO;
        if (Objects.equals(unidadeDevolucao.getCodigo(), subprocesso.getUnidade().getCodigo())) {
            novaSituacao = SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO;
            subprocesso.setDataFimEtapa1(null);
            diagnostico.setSituacao(SituacaoDiagnostico.EM_ANDAMENTO);
            diagnostico.setDataConclusao(null);
            diagnostico.setJustificativaConclusao(null);
        }

        Usuario usuario = usuarioFacade.usuarioAutenticado();
        transicaoService.registrarAnaliseSemEmail(RegistrarWorkflowCommand.builder()
                .sp(subprocesso)
                .novaSituacao(novaSituacao)
                .tipoTransicao(TipoTransicao.DIAGNOSTICO_DEVOLVIDO)
                .tipoAnalise(TipoAnalise.DIAGNOSTICO)
                .tipoAcaoAnalise(TipoAcaoAnalise.DEVOLUCAO_DIAGNOSTICO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeOrigemTransicao(unidadeAnalise)
                .unidadeDestinoTransicao(unidadeDevolucao)
                .usuario(usuario)
                .motivoAnalise(null)
                .observacoes(observacao)
                .build());

        notificacaoService.notificarDiagnosticoDevolvido(subprocesso, unidadeAnalise, unidadeDevolucao);
    }

    public void validarDiagnostico(Long codSubprocesso, @Nullable String observacao) {
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));
        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        Unidade unidadeAnalise = localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso);
        Unidade unidadeSuperior = buscarSuperiorImediato(unidadeAnalise.getCodigo());
        if (unidadeSuperior == null) {
            return;
        }

        diagnostico.setSituacao(SituacaoDiagnostico.VALIDADO);

        Usuario usuario = usuarioFacade.usuarioAutenticado();
        transicaoService.registrarAnaliseSemEmail(RegistrarWorkflowCommand.builder()
                .sp(subprocesso)
                .novaSituacao(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO)
                .tipoTransicao(TipoTransicao.DIAGNOSTICO_ACEITO)
                .tipoAnalise(TipoAnalise.DIAGNOSTICO)
                .tipoAcaoAnalise(TipoAcaoAnalise.ACEITE_DIAGNOSTICO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeOrigemTransicao(unidadeAnalise)
                .unidadeDestinoTransicao(unidadeSuperior)
                .usuario(usuario)
                .motivoAnalise(null)
                .observacoes(observacao)
                .build());

        notificacaoService.notificarDiagnosticoAceito(subprocesso, unidadeAnalise, unidadeSuperior);
    }

    public void homologarDiagnostico(Long codSubprocesso, @Nullable String observacao) {
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));
        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        diagnostico.setSituacao(SituacaoDiagnostico.HOMOLOGADO);

        Usuario usuario = usuarioFacade.usuarioAutenticado();
        Unidade admin = unidadeService.buscarAdmin();
        transicaoService.registrarTransicaoSemEmail(RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(TipoTransicao.DIAGNOSTICO_HOMOLOGADO)
                .origem(admin)
                .destino(admin)
                .usuario(usuario)
                .observacoes(observacao)
                .build());

        notificacaoService.notificarDiagnosticoHomologado(subprocesso);
    }

    private Unidade obterUnidadeDevolucao(sgc.subprocesso.model.Subprocesso subprocesso, Unidade unidadeAnalise) {
        List<Movimentacao> movimentacoes = subprocessoConsultaService.listarMovimentacoesOrdenadas(subprocesso.getCodigo());

        return movimentacoes.stream()
                .filter(movimentacao -> Objects.equals(movimentacao.getUnidadeDestino().getCodigo(), unidadeAnalise.getCodigo()))
                .map(Movimentacao::getUnidadeOrigem)
                .filter(unidadeOrigem -> hierarquiaService.isSubordinada(unidadeOrigem, unidadeAnalise))
                .findFirst()
                .orElseThrow(() -> new sgc.comum.erros.ErroInconsistenciaInterna(
                        "Historico de movimentacoes inconsistente para devolucao do subprocesso %s na unidade %s"
                                .formatted(subprocesso.getCodigo(), unidadeAnalise.getCodigo())
                ));
    }

    private @Nullable Unidade buscarSuperiorImediato(Long codigoUnidade) {
        Long codigoPai = unidadeHierarquiaService.buscarCodigoPai(codigoUnidade);
        if (codigoPai == null) {
            return null;
        }
        return unidadeService.buscarPorCodigo(codigoPai);
    }
}
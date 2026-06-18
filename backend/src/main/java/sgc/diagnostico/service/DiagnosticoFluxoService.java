package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.model.ComumRepo;
import sgc.diagnostico.model.Diagnostico;
import sgc.diagnostico.model.DiagnosticoRepo;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.HierarquiaService;
import sgc.organizacao.service.UnidadeHierarquiaService;
import sgc.organizacao.service.UnidadeService;
import sgc.organizacao.service.UsuarioService;
import sgc.processo.model.ServidorProcesso;
import sgc.processo.model.UnidadeProcesso;
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
import sgc.diagnostico.model.SituacaoCapacitacao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class DiagnosticoFluxoService {
    private final DiagnosticoRepo diagnosticoRepo;
    private final ComumRepo repo;
    private final DiagnosticoValidacaoService validacaoService;
    private final DiagnosticoNotificacaoService notificacaoService;
    private final SubprocessoConsultaService subprocessoConsultaService;
    private final SubprocessoTransicaoService transicaoService;
    private final SubprocessoValidacaoService subprocessoValidacaoService;
    private final LocalizacaoSubprocessoService localizacaoSubprocessoService;
    private final UnidadeService unidadeService;
    private final UnidadeHierarquiaService unidadeHierarquiaService;
    private final HierarquiaService hierarquiaService;
    private final DiagnosticoUsuarioContextoService usuarioContextoService;
    private final UsuarioService usuarioService;

    public void inicializarDiagnostico(Subprocesso subprocesso) {
        Diagnostico diagnostico = Diagnostico.builder()
                .subprocesso(subprocesso)
                .build();

        List<ServidorProcesso> servidoresSnapshot = subprocesso.getProcesso()
                .buscarServidoresParticipantes(subprocesso.getUnidade().getCodigo());
        List<Usuario> servidores = carregarServidoresSnapshot(servidoresSnapshot);
        List<AvaliacaoServidor> avaliacoes = new ArrayList<>();
        List<SituacaoCapacitacao> situacaoCapacitacoes = new ArrayList<>();

        Mapa mapa = subprocesso.getMapa();
        if (mapa != null) {
            UnidadeProcesso unidadeSnapshot = subprocesso.getProcesso()
                    .buscarParticipante(subprocesso.getUnidade().getCodigo())
                    .orElse(null);
            for (Usuario servidor : servidores) {
                ServidorProcesso servidorSnapshot = localizarSnapshotObrigatorio(servidoresSnapshot, servidor.getTituloEleitoral());
                for (Competencia competencia : mapa.getCompetencias()) {
                    AvaliacaoServidor avaliacao = AvaliacaoServidor.builder()
                            .diagnostico(diagnostico)
                            .servidor(servidor)
                            .servidorNomeSnapshot(servidorSnapshot.getNome())
                            .competencia(competencia)
                            .situacaoServidor(SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA)
                            .build();
                    avaliacoes.add(avaliacao);

                    situacaoCapacitacoes.add(SituacaoCapacitacao.builder()
                            .diagnostico(diagnostico)
                            .servidor(servidor)
                            .servidorNomeSnapshot(servidorSnapshot.getNome())
                            .unidadeCodigoSnapshot(unidadeSnapshot != null ? unidadeSnapshot.getUnidadeCodigoPersistido() : subprocesso.getUnidade().getCodigo())
                            .unidadeSiglaSnapshot(unidadeSnapshot != null ? unidadeSnapshot.getSigla() : subprocesso.getUnidade().getSigla())
                            .unidadeNomeSnapshot(unidadeSnapshot != null ? unidadeSnapshot.getNome() : subprocesso.getUnidade().getNome())
                            .competencia(competencia)
                            .build());
                }
            }
        }

        diagnostico.setAvaliacaoServidores(avaliacoes);
        diagnostico.setSituacaoCapacitacoes(situacaoCapacitacoes);
        diagnosticoRepo.save(diagnostico);
    }

    private List<Usuario> carregarServidoresSnapshot(List<ServidorProcesso> servidoresSnapshot) {
        List<String> titulos = servidoresSnapshot.stream()
                .map(ServidorProcesso::getUsuarioTitulo)
                .distinct()
                .toList();
        if (titulos.isEmpty()) {
            return List.of();
        }
        var usuariosPorTitulo = new HashMap<String, Usuario>();
        usuarioService.buscarPorTitulos(titulos).forEach(usuario -> usuariosPorTitulo.put(usuario.getTituloEleitoral(), usuario));
        return titulos.stream()
                .map(titulo -> {
                    Usuario usuario = usuariosPorTitulo.get(titulo);
                    if (usuario == null) {
                        throw new sgc.comum.erros.ErroInconsistenciaInterna(
                                "Servidor %s do snapshot do processo não foi encontrado para inicializar o diagnóstico".formatted(titulo)
                        );
                    }
                    return usuario;
                })
                .toList();
    }

    private ServidorProcesso localizarSnapshotObrigatorio(List<ServidorProcesso> snapshots, String tituloEleitoral) {
        return snapshots.stream()
                .filter(snapshot -> Objects.equals(snapshot.getUsuarioTitulo(), tituloEleitoral))
                .findFirst()
                .orElseThrow(() -> new sgc.comum.erros.ErroInconsistenciaInterna(
                        "Servidor %s não encontrado no snapshot do processo".formatted(tituloEleitoral)
                ));
    }

    public void concluirDiagnosticoUnidade(Long codSubprocesso) {
        Diagnostico diagnostico = repo.buscar(Diagnostico.class, java.util.Map.of("subprocesso.codigo", codSubprocesso));
        validacaoService.validarConclusaoUnidade(diagnostico.getCodigo());

        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO);

        diagnostico.setDataConclusao(LocalDateTime.now());
        diagnostico.setJustificativaConclusao(null);

        subprocesso.setSituacao(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        subprocesso.setDataFimEtapa1(LocalDateTime.now());

        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        Unidade unidadeOrigem = subprocesso.getUnidade();
        Unidade unidadeDestino = buscarSuperiorImediato(unidadeOrigem.getCodigo());
        if (unidadeDestino == null) {
            // Caso ocorra na unidade ADMIN (topo da hierarquia), que não possui superior imediato
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

    public void validarConclusaoDiagnosticoUnidade(Long codSubprocesso) {
        Diagnostico diagnostico = repo.buscar(Diagnostico.class, java.util.Map.of("subprocesso.codigo", codSubprocesso));
        validacaoService.validarConclusaoUnidade(diagnostico.getCodigo());
        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO);
    }

    public void devolverDiagnostico(Long codSubprocesso, @Nullable String observacao) {
        Diagnostico diagnostico = repo.buscar(Diagnostico.class, java.util.Map.of("subprocesso.codigo", codSubprocesso));
        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        Unidade unidadeAnalise = localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso);
        Unidade unidadeDevolucao = obterUnidadeDevolucao(subprocesso, unidadeAnalise)
                .orElseThrow(() -> new sgc.comum.erros.ErroInconsistenciaInterna(
                        "Historico de movimentacoes inconsistente para devolucao do subprocesso %s na unidade %s"
                                .formatted(subprocesso.getCodigo(), unidadeAnalise.getCodigo())
                ));

        SituacaoSubprocesso novaSituacao = SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO;
        if (Objects.equals(unidadeDevolucao.getCodigo(), subprocesso.getUnidade().getCodigo())) {
            novaSituacao = SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO;
            subprocesso.setDataFimEtapa1(null);
            diagnostico.setDataConclusao(null);
            diagnostico.setJustificativaConclusao(null);
            diagnostico.getAvaliacaoServidores()
                    .forEach(avaliacao -> avaliacao.setSituacaoServidor(SituacaoAvaliacaoServidor.CONSENSO_CRIADO));
        }

        Usuario usuario = usuarioContextoService.usuarioAutenticado();
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

        notificacaoService.notificarDiagnosticoDevolvido(subprocesso, unidadeAnalise, unidadeDevolucao, observacao);
    }

    public void validarDevolucaoDiagnostico(Long codSubprocesso) {
        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        Unidade unidadeAnalise = localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso);
        obterUnidadeDevolucao(subprocesso, unidadeAnalise)
                .orElseThrow(() -> new sgc.comum.erros.ErroInconsistenciaInterna(
                        "Historico de movimentacoes inconsistente para devolucao do subprocesso %s na unidade %s"
                                .formatted(subprocesso.getCodigo(), unidadeAnalise.getCodigo())
                ));
    }

    public void validarDiagnostico(Long codSubprocesso, @Nullable String observacao) {
        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        Unidade unidadeAnalise = localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso);
        Unidade unidadeSuperior = buscarSuperiorImediato(unidadeAnalise.getCodigo());
        if (unidadeSuperior == null) {
            return;
        }

        Usuario usuario = usuarioContextoService.usuarioAutenticado();
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

    public void validarDiagnosticosEmBloco(List<Long> subprocessoCodigos) {
        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        List<Subprocesso> subprocessos = subprocessoCodigos.stream()
                .map(subprocessoConsultaService::buscarSubprocesso)
                .toList();

        for (Subprocesso subprocesso : subprocessos) {
            validarDiagnosticoEmBloco(subprocesso, usuario);
        }

        notificacaoService.notificarDiagnosticosAceitosEmBloco(subprocessos);
    }

    public void validarAceiteDiagnostico(Long codSubprocesso) {
        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
    }

    public void homologarDiagnostico(Long codSubprocesso, @Nullable String observacao) {
        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        validacaoService.validarDiagnosticoHomologavel(codSubprocesso);

        subprocesso.setSituacao(SituacaoSubprocesso.DIAGNOSTICO_HOMOLOGADO);

        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        Unidade admin = unidadeService.buscarAdmin();
        transicaoService.registrarAnaliseSemEmail(RegistrarWorkflowCommand.builder()
                .sp(subprocesso)
                .novaSituacao(SituacaoSubprocesso.DIAGNOSTICO_HOMOLOGADO)
                .tipoTransicao(TipoTransicao.DIAGNOSTICO_HOMOLOGADO)
                .tipoAnalise(TipoAnalise.DIAGNOSTICO)
                .tipoAcaoAnalise(TipoAcaoAnalise.HOMOLOGACAO_DIAGNOSTICO)
                .unidadeAnalise(admin)
                .unidadeOrigemTransicao(admin)
                .unidadeDestinoTransicao(admin)
                .usuario(usuario)
                .observacoes(observacao)
                .build());

        notificacaoService.notificarDiagnosticoHomologado(subprocesso);
    }

    public void validarHomologacaoDiagnostico(Long codSubprocesso) {
        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        validacaoService.validarDiagnosticoHomologavel(codSubprocesso);
    }

    private Optional<Unidade> obterUnidadeDevolucao(sgc.subprocesso.model.Subprocesso subprocesso, Unidade unidadeAnalise) {
        List<Movimentacao> movimentacoes = subprocessoConsultaService.listarMovimentacoesOrdenadas(subprocesso.getCodigo());

        return movimentacoes.stream()
                .filter(movimentacao -> Objects.equals(movimentacao.getUnidadeDestino().getCodigo(), unidadeAnalise.getCodigo()))
                .map(Movimentacao::getUnidadeOrigem)
                .filter(unidadeOrigem -> hierarquiaService.isSubordinada(unidadeOrigem, unidadeAnalise))
                .findFirst();
    }

    private @Nullable Unidade buscarSuperiorImediato(Long codigoUnidade) {
        Long codigoPai = unidadeHierarquiaService.buscarCodigoPai(codigoUnidade);
        if (codigoPai == null) {
            return null;
        }
        return unidadeService.buscarPorCodigo(codigoPai);
    }

    private void validarDiagnosticoEmBloco(Subprocesso subprocesso, Usuario usuario) {
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        Unidade unidadeAnalise = localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso);
        Unidade unidadeSuperior = buscarSuperiorImediato(unidadeAnalise.getCodigo());
        if (unidadeSuperior == null) {
            return;
        }

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
                .observacoes("Avaliação em bloco")
                .build());
    }
}
